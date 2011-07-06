'''
Created on Apr 20, 2011

@author: mrrussell
'''
import datetime
import logging
import os
import subprocess
import util
import re

global eclipseArchiveDir
eclipseArchiveDir = os.path.join(os.sep + "home", "data", "httpd", 
                                 "download.eclipse.org", "eclipse",
                                 "downloads", "drops")
global eclipseArchiveFile
eclipseArchiveFile = None

global eclipseDir
eclipseDir = None

log = logging.getLogger("releng.eclipse")

def __findEclipseArchive(eclipseVersion):
  global eclipseArchiveFile
  log.debug("findEclipseArchive(" + eclipseVersion + ")")
  log.info("searching for Eclipse in " + eclipseArchiveDir)
  if eclipseArchiveFile == None:
    try:
      files = os.listdir(eclipseArchiveDir);
    except OSError as e:
      log.error("could not read files in " + eclipseArchiveDir);
      raise e
  
    foundEclipseDirs = []
    for file in files:
      if file.startswith("R-") or file.startswith("S-"):
        log.debug('found: ' + file)
        foundEclipseDirs.append(file)
    if len(foundEclipseDirs) == 0:
      log.error("could not find any Eclipse directories starting with R or S in " +
                eclipseArchiveDir)
      raise OSError("could not find any Eclipse directories starting with R or S in " +
                eclipseArchiveDir)
      
    savedTs = datetime.datetime(1970,1,1)
    searchDir = None
    searchTerm = "[RS]-([0-9]\.[0-9](\.[0-9]|M[0-9]|RC[0-9])?)-(.+)(.*)"
    search = re.compile(searchTerm)
    for dir in foundEclipseDirs:
      results = search.search(dir)
      util.__displaymatch(results)
      version = results.group(1)
      timestamp = results.group(3)
      if version.find(eclipseVersion) >= 0:
        eclipseTs = datetime.datetime.strptime(timestamp, "%Y%m%d%H%M")
        if eclipseTs > savedTs:
          savedTs = eclipseTs
          searchDir = os.path.join(eclipseArchiveDir, dir)

    log.debug('searchDir = ' + searchDir);
    if searchDir == None:
      log.error("could not find any Eclipse directories with " + eclipseVersion)
      raise OSError("could not find any Eclipse directories with " + eclipseVersion)
    
    try:
      files = os.listdir(searchDir);
    except OSError as e:
      log.error("could not read files in " + searchDir);
      raise e
  
    eclipseArchiveFile = None
    searchTerm = eclipseVersion + "(M[0-9]|RC[0-9]|\.[0-3])?-linux-gtk.tar.gz"
    search = re.compile(searchTerm)

    log.debug('searchTerm = ' + searchTerm)
    for file in files:
      if search.search(file) != None:
        eclipseArchiveFile = os.path.join(searchDir, file)
        break;
  
    if eclipseArchiveFile == None:
      log.error("could not find any Eclipse archives with " + searchTerm + ' in ' + file)
      raise OSError("could not find any Eclipse archives with " + searchTerm)
    
  log.info("found Eclipse archive " + eclipseArchiveFile)
  return eclipseArchiveFile

def __getEclipse(version, dir):
  if eclipseArchiveFile == None:
    eclipseArchive = __findEclipseArchive(version);
    util.unarchive(eclipseArchive, dir)
  return os.path.join(dir, "eclipse")
  

def __findPlugin(eclipseHome, search):
  log.debug("findPlugin(" + search + ")")
  plugins = os.path.join(eclipseHome, "plugins")
  try:
    files = os.listdir(plugins);
  except OSError as e:
    log.error("could not read files in " + eclipseArchiveDir);
    raise e

  for file in files:
    if file.startswith(search):
      return os.path.join(plugins, file)
  
  raise OSError("could not find plugin starting with " + search)

def optimizeSite(baseDir, dir, eclipseVersion):
  log.debug("optimizeSite(" + baseDir + ", " + dir + ", " + eclipseVersion + ")")
  eclipseHome = __getEclipse(eclipseVersion, baseDir);
  try:
    os.mkdir(baseDir)
  except OSError as e:
    if e.errno != 17:
      raise e

  launcher = __findPlugin(eclipseHome, "org.eclipse.equinox.p2.jarprocessor")
  try:
    files = os.listdir(dir);
  except OSError as e:
    log.error("could not read files in " + dir);
    raise e

  out = os.path.join(dir, "out")
  for file in files:
    fullFile = os.path.join(dir, file);
    commands = ["java", "-jar", launcher, "-processAll", "-repack", 
                "-outputDir", out, fullFile]
    for cmd in commands:
      print cmd,
    print
    subprocess.check_call(commands)

  return out
  
def packSite(baseDir, dir, eclipseVersion):
  log.debug("packSite(" + baseDir + ", " + dir + ", " + eclipseVersion + ")")
  eclipseHome = __getEclipse(eclipseVersion, baseDir);
  try:
    os.mkdir(baseDir)
  except OSError as e:
    if e.errno != 17:
      raise e
   
  launcher = __findPlugin(eclipseHome, "org.eclipse.equinox.p2.jarprocessor")
  try:
    files = os.listdir(dir);
  except OSError as e:
    log.error("could not read files in " + dir);
    raise e

  out = os.path.join(dir, "out")
  for file in files:
    fullFile = os.path.join(dir, file);
    commands = ["java", "-jar", launcher, "-processAll", "-pack", 
                "-outputDir", out, fullFile]
    for cmd in commands:
      print cmd,
    print
    subprocess.check_call(commands)

  return out

def publishSite(baseDir, dir, eclipseVersion):
  log.debug("publishSite(" + baseDir + ", " + dir + ", " + eclipseVersion + ")")
  eclipseHome = __getEclipse(eclipseVersion, baseDir);
   
  launcher = __findPlugin(eclipseHome, "org.eclipse.equinox.launcher_")

  out = os.path.join(baseDir, "out")
  for eclipseVer in ['3.4', '3.5', '3.6', '3.7']:
    fullFile = os.path.join(dir, eclipseVer);
    if os.path.exists(fullFile):
      os.remove(os.path.join(fullFile, 'artifacts.jar'))
      os.remove(os.path.join(fullFile, 'content.jar'))
      target = "file:" + fullFile
      commands = ["java", "-jar", launcher, "-application",  
                  "org.eclipse.equinox.p2.publisher.UpdateSitePublisher", 
                  "-metadataRepository", target, 
                  "-metadataRepositoryName",  
                  "Eclipse " + eclipseVer + " WindowBuilder Repo",
                  "-artifactRepository", target,
                  "-artifactRepositoryName",
                  "Eclipse " + eclipseVer + " WindowBuilder Repo",
                  "-source", fullFile,
                  "-compress", "-publishArtifacts"]
      for cmd in commands:
        print cmd,
      print
      subprocess.check_call(commands)

  return out

def setArchiveDir(dir):
  global eclipseArchiveDir
  eclipseArchiveDir = dir  

