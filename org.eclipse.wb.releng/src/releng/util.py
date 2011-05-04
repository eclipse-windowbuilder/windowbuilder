'''
Created on Apr 20, 2011

@author: mrrussell
'''
import io
import logging
import os
import subprocess

log = logging.getLogger("releng.util")

def unarchive(archive, dest):
  log.debug("unarchive(" + archive + ", " + dest + ")")
  cwd = os.getcwd()
  os.chdir(dest)
  fhLogFile = open('/dev/null', 'wt')
  if archive.endswith("tar.gz") or archive.endswith("tgz"):
    subprocess.check_call(['/bin/tar', 'xzvf', archive], stdout = fhLogFile)
  elif archive.endswith("tar"):
    subprocess.check_call(['/bin/tar', 'xvf', archive], stdout = fhLogFile)
  elif archive.endswith("zip"):
    subprocess.check_call(['/usr/bin/unzip', archive], stdout = fhLogFile)
  
  os.chdir(cwd)

def updateMd5Hash(dir):
  log.debug("updateMd5Hash(" + dir + ")")
  try:
    files = os.listdir(dir);
  except OSError as e:
    log.error("could not read files in " + dir);
    raise e

  for file in files:
    if file.endswith('.zip'):
      fullFile = os.path.join(dir, file);
      commands = ["/usr/bin/md5sum", "-b", fullFile]
      if log.info:
        cl = ""
        for cmd in commands:
          cl = cl + cmd + ' '
        log.info('command line: ' + cl)
      
      md5File = os.path.join(dir, file + '.MD5')
      fhMd5File = open(md5File, 'wt')
      log.info('creating ' + md5File)
      subprocess.check_call(commands, stdout = fhMd5File)
      
