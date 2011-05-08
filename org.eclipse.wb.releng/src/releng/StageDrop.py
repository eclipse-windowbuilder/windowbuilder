"""
Python script to stage a drop of WindowBuilder
"""
import datetime
import eclipse
import logging
import logging.config
import os
import re
import shutil
import stat
import subprocess
import sys
import util
import zipfile 

from optparse import OptionParser
from time import sleep
from datetime import datetime, time, date

logging.config.fileConfig('logger.config')

log = logging.getLogger("releng")
log.info("starting StageDrop.py")

def main():
  log.debug("in main")
  data = processArgs()
  dropLocation = data['droplocation']
  subproduct = data['subproduct']
  signDir = data['signdir']
  eclipseVersion = data['eclipseversion']
  optimizeSite = data['optimizesite']
  packSite = data['packsite']
  signFiles = data['signfiles']  
  doDeploy = data['dodeploy']
  baseDir = initialize(subproduct)
  
  productDir = os.path.join(baseDir, subproduct);
  
  log.info("clear directory " + signDir)
  rmDirTree(signDir)
  log.info("Copy files from " + dropLocation + " to " + productDir)
  copyFiles(dropLocation, productDir, None)
  
  log.info("Move zip files from " + productDir + " to " + signDir)
  moveFiles(productDir, signDir, zipFilter)
  
  if optimizeSite:
    log.info("Optimize Site")
    optimizedDir = eclipse.optimizeSite(baseDir, signDir, eclipseVersion)
    copyFiles(optimizedDir, signDir, None)
    rmDirTree(optimizedDir)
    os.rmdir(optimizedDir)
    
  if signFiles:
    log.info("Sign files")
    signedDir = signZipFiles(signDir)
    copyFiles(signedDir, signDir, None)
    rmDirTree(signedDir)
    os.rmdir(signedDir)

  if packSite:
    log.info("pack Site")
    packDir = eclipse.packSite(baseDir, signDir, eclipseVersion)
    copyFiles(packDir, signDir, None)
    rmDirTree(packDir)
    os.rmdir(packDir)
  
  log.info("Move signed files from " + signDir + " to " + productDir)
  moveFiles(signDir, productDir, None)
  
  log.info("Unzip the signed files")
  unzipSites(productDir)
  
  log.info("Generate Eclipse P2 Metadata")
  eclipse.publishSite(baseDir, productDir, eclipseVersion)
  
  log.info("rezip Site")
  rezipSite(productDir)
  
  log.info("update MD5 files")
  util.updateMd5Hash(productDir)

  if doDeploy:
    log.info("deploy code")
    deployCode(productDir)

  log.info("cleanup")
  cleanup(signDir)
  
  log.debug("done main")
  

def zipFilter(file):
  return file.endswith('.zip')

def processArgs():
  signDir = os.path.join(os.sep + "home", "data", "httpd", 
                         "download-staging.priv", "tools", "windowbuilder")
  usage = "usage: %prog [options] drop subproduct"
  parser = OptionParser(usage=usage)
  parser.set_defaults(debug=False)
  parser.set_defaults(eclipseversion="3.7")
  parser.set_defaults(optimizesite=True)
  parser.set_defaults(packsite=True)
  parser.set_defaults(signfiles=True)
  parser.set_defaults(dodeploy=False)
  parser.add_option("--signdir", action="store", dest="signdir")
  parser.add_option("-e", "--eclipseversion", action="store", 
                    dest="eclipseversion")
  parser.add_option("--eclipsearchivedir", action="store", 
                    dest="eclipsearchivedir")
  parser.add_option("--nooptimizesite", action="store_false", dest="optimizesite");
  parser.add_option("--nopacksite", action="store_false", dest="packsite")
  parser.add_option("--nosignfiles", action="store_false", dest="signfiles")
  parser.add_option("--deployfiles", action="store_true", dest="dodeploy")
  (options, args) = parser.parse_args()
  
  if len(args) != 2:
    parser.error("incorrect number of arguments")
    
  optimizeSite = options.optimizesite
  packSite = options.packsite
  signFiles = options.signfiles
  doDeploy = options.dodeploy

  if options.signdir != None:
    signDir = options.signdir
  
  if options.eclipsearchivedir != None:
    eclipse.setArchiveDir(options.eclipsearchivedir)
    
  eclipseVersion = options.eclipseversion
       
  dropLocation = args[0]
  subproduct = args[1]
  
  if dropLocation == None:
    log.error("you must specify a drop location")
    usage()
    sys.exit(20)

  if subproduct == None:
    log.error("you must specify a subproduct")
    usage()
    sys.exit(21)
  
  ret = dict({'droplocation':dropLocation, 'subproduct':subproduct, 
              'signdir':signDir, 'eclipseversion':eclipseVersion,
              'optimizesite':optimizeSite, 'packsite':packSite,
              'signfiles':signFiles, 'dodeploy':doDeploy})
  log.debug("out of processArgs")
  return ret

def initialize(subproduct):
  log.debug("initialize")
  baseDir = os.path.join(os.sep + "shared", "tools", "windowbuilder", "stage")
  
  WBDir = os.path.join(baseDir, subproduct)
  rmDirTree(baseDir)
  try:
    os.mkdir(baseDir)
  except OSError as e:
    if e.errno != 17:
      log.error("could not create " + baseDir)
      raise e
  
  os.mkdir(WBDir)
  return baseDir
  
def rmDirTree(top):
  # Delete everything reachable from the directory named in "top",
  # assuming there are no symbolic links.
  # CAUTION:  This is dangerous!  For example, if top == '/', it
  # could delete all your disk files.
  log.debug("rmDirTree(" + top + ")")
  log.info("removing " + top + " directory tree")
  if top == "/":
    log.critical("can not pass / as the top directory")
    return
  
  for root, dirs, files in os.walk(top, topdown=False):
    for name in files:
        os.remove(os.path.join(root, name))
    for name in dirs:
        os.rmdir(os.path.join(root, name))
  return

def copyFiles(fromDir, toDir, filter):
  log.debug("copyFiles(" + fromDir + ", " + toDir)
  try:
    files = os.listdir(fromDir);
  except OSError as e:
    log.error("could not read files in " + fromDir);
    raise e
  
  if len(files) == 0:
    if (filter == None or filter(file)):
      raise OSError("no files to process")
  
  for file in files:
    shutil.copy2(os.path.join(fromDir, file), toDir)
    
def moveFiles(fromDir, toDir, filter):
  log.debug("moveFiles(" + fromDir + ", " + toDir)
  try:
    files = os.listdir(fromDir);
  except OSError as e:
    log.error("could not read files in " + fromDir);
    raise e
  
  if len(files) == 0:
    raise OSError("no files to process")
  
  for file in files:
    if (filter == None or filter(file)):
      shutil.move(os.path.join(fromDir, file), toDir)
    
def signZipFiles(dir):
  log.debug("signFiles(" + dir + ")")
  
  try:
    files = os.listdir(dir);
  except OSError as e:
    log.error("could not read files in " + dir);
    raise e
  
  filesToSign = []
  for file in files:
    if file.endswith('.zip'):
      zipPath = os.path.join(dir, file)
      os.chmod(zipPath, stat.S_IWRITE | stat.S_IREAD | stat.S_IWGRP | 
               stat.S_IRGRP | stat.S_IWOTH | stat.S_IROTH)
#      subprocess.check_call(['/bin/echo', 'sign', zipPath, 'nomail', 'signed'])
      subprocess.check_call(['/usr/local/bin/sign', zipPath, 'nomail', 'signed'])
      filesToSign.append(os.path.join(dir, "signed", file))

  signedFiles = []
  found = False;
  while(not found):
    found = True
    for x in range(60):
      sleep(1)
    for file in filesToSign:
      if (os.path.exists(file)):
        log.debug(file + " exists")
        signedFiles.append(file)
      else:
        found = False
        log.debug(file + " does not exists")
        continue
        
  sleep(10)
  log.info("all files have been signed")
  return os.path.join(dir, "signed")


def unzipSites(dir):
  log.debug("unzipSites(" + dir + ")")
  try:
    files = os.listdir(dir);
  except OSError as e:
    log.error("could not read files in " + dir);
    raise e
  versionRE = re.compile('.+Eclipse([0-9]\.[0-9]).+')
  
  for file in files:
    res = versionRE.search(file)
    __displaymatch(res)
    version = res.group(1)
    unarchiveDest = os.path.join(dir, version)
    if not os.path.exists(unarchiveDest):
      os.mkdir(unarchiveDest)
      
    util.unarchive(os.path.join(dir, file), unarchiveDest)

def __displaymatch(match):
    if match is None:
        return None
    log.debug('<Match: %r, groups=%r>' % (match.group(), match.groups()))
      
def rezipSite(dir):
  log.debug("rezipSite(" + dir + ")")
  formatDir =  'D: {0:<120} N: {1}'
  formatFile = 'F: {0:<120} N: {1}'
  formatZip = '{0:<70} {1:>9} {2:>9}'
  try:
    files = os.listdir(dir);
  except OSError as e:
    log.error("could not read files in " + dir);
    raise e
  versionRE = re.compile('.+Eclipse([0-9]\.[0-9]).+')
  
  for file in files:
    if file.endswith('.zip'):
      zipFile = os.path.join(dir, file)
      log.info("processing " + zipFile)
      res = versionRE.search(file)
      __displaymatch(res)
      version = res.group(1)
      siteDir = os.path.join(dir, version)

      log.debug("removing " + zipFile)
      os.remove(zipFile)
      log.debug("creating zip file " + zipFile)
      cwd = os.getcwd()
      os.chdir(siteDir)
      log.debug('creating zip in ' + os.getcwd())
      command = ['zip', zipFile]

      for root, dirs, files in os.walk(siteDir, followlinks=False):
        for name in files:
          fileToZip = os.path.join(root, name)
          zipFileName = fileToZip[len(siteDir)+1:]
          log.debug(formatFile.format(fileToZip, zipFileName))
          command.append(zipFileName)
            
      if log.debug:
        data = "Command: "
        for cmd in command:
          data = data + cmd + ' '
        log.debug(data)

      subprocess.check_call(command)
      # open the file again, to see what's in it
      if (log.debug):
        zip = zipfile.ZipFile(zipFile, "r")
        for info in zip.infolist():
          log.debug(formatZip.format(info.filename, info.file_size, info.compress_size))
    log.debug("out rezipSite")

def deployCode(dir):
  log.debug("in deployCode(" + dir + ")")
  deployDir = os.path.join(os.sep + 'home', 'data', 'httpd', 'download.eclipse.org', 'tools', 'windowbuilder')
  latestDir = os.path.join(deployDir, 'latest')
  d = datetime.today()
  nowString = '{%Y%m%d%H%M}'.format(d)
  dateDir = os.path.join(deployDir, nowString)
  log.info("deploying to ")
  log.info(latestDir)
  log.info('and')
  log.infor(dateDir)
  log.debug("out deployCode")

def cleanup(signDir):
  log.debug("in cleanup(" + signDir + ")")
  log.debug("out cleanup")

if __name__ == "__main__":
  main()
  log.info("StageDrop.py is done")
