"""Copyright 2011 Google Inc. All Rights Reserved.

All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html

Contributors:
  Google, Inc. - initial API and implementation

@author: Mark R Russell
"""


import datetime
import logging
import os
import re
import subprocess
import util

global eclipse_archive_dir
eclipse_archive_dir = None
global eclipse_archive_file
eclipse_archive_file = None
global eclipse_dir
eclipse_dir = None

eclipse_archive_dir = os.path.join(os.sep + 'home', 'data', 'httpd',
                                   'download.eclipse.org', 'eclipse',
                                   'downloads', 'drops')

log = logging.getLogger('releng.eclipse')


def _FindEclipseArchive(eclipse_version):
  global eclipse_archive_file
  log.debug('findEclipseArchive(' + eclipse_version + ')')
  log.info('searching for Eclipse in ' + eclipse_archive_dir)
  if eclipse_archive_file is None:
    try:
      files = os.listdir(eclipse_archive_dir)
    except OSError as e:
      log.error('could not read files in ' + eclipse_archive_dir)
      raise e

    found_eclipse_dirs = []
    for f in files:
      if f.startswith('R-') or f.startswith('S-'):
        log.debug('found: ' + f)
        found_eclipse_dirs.append(f)
    if not found_eclipse_dirs:
      msg = ('could not find any Eclipse directories'
             ' starting with R or S in {0}').format(eclipse_archive_dir)
      log.error(msg)
      raise OSError(msg)

    saved_ts = datetime.datetime(1970, 1, 1)
    search_dir = None
    search_term = '[RS]-([0-9]\.[0-9](\.[0-9]|M[0-9]|RC[0-9])?)-(.+)(.*)'
    search = re.compile(search_term)
    for d in found_eclipse_dirs:
      results = search.search(d)
      util.DisplayMatch(results)
      version = results.group(1)
      timestamp = results.group(3)
      if version.find(eclipse_version) >= 0:
        eclipse_ts = datetime.datetime.strptime(timestamp, '%Y%m%d%H%M')
        if eclipse_ts > saved_ts:
          saved_ts = eclipse_ts
          search_dir = os.path.join(eclipse_archive_dir, d)

    log.debug('search_dir = ' + search_dir)
    if search_dir is None:
      msg = 'could not find any Eclipse directories with ' + eclipse_version
      log.error(msg)
      raise OSError(msg)

    try:
      files = os.listdir(search_dir)
    except OSError as e:
      log.error('could not read files in ' + search_dir)
      raise e

    eclipse_archive_file = None
    search_term = eclipse_version + '(M[0-9]|RC[0-9]|\.[0-3])?-linux-gtk.tar.gz'
    search = re.compile(search_term)

    log.debug('search_term = ' + search_term)
    for f in files:
      if search.search(f) is not None:
        eclipse_archive_file = os.path.join(search_dir, f)
        break

    if eclipse_archive_file is None:
      msg = ('could not find any Eclipse archives'
             ' with {0} in {1}').format(search_term, search_dir)
      log.error(msg)
      raise OSError(msg)

  log.info('found Eclipse archive ' + eclipse_archive_file)
  return eclipse_archive_file


def _GetEclipse(version, directory):
  if eclipse_archive_file is None:
    eclipse_archive = _FindEclipseArchive(version)
    util.Unarchive(eclipse_archive, directory)
  return os.path.join(directory, 'eclipse')


def _FindPlugin(eclipse_home, search):
  log.debug('_FindPlugin(' + search + ')')
  plugins = os.path.join(eclipse_home, 'plugins')
  try:
    files = os.listdir(plugins)
  except OSError as e:
    log.error('could not read files in ' + eclipse_archive_dir)
    raise e

  for f in files:
    if f.startswith(search):
      return os.path.join(plugins, f)

  raise OSError('could not find plugin starting with ' + search)


def OptimizeSite(eclipse_install_dir, ziped_update_sites_dir, eclipse_version):
  """Call the Eclipse Site optimizer to optimize the update site.

  Args:
    eclipse_install_dir: directory to unarchive Eclipse if it has not
                          been unarchived yet
    ziped_update_sites_dir: the directory where the zipped update sites
                            are stored
    eclipse_version: the version of eclipse to use for optimizing

  Returns:
    the new directory where the optimized zipfiles were written
  """
  log.debug('optimizeSite({0}, {1}, {2}'.format(eclipse_install_dir,
                                                ziped_update_sites_dir,
                                                eclipse_version))
  eclipse_home = _GetEclipse(eclipse_version, eclipse_install_dir)
  try:
    os.mkdir(eclipse_install_dir)
  except OSError as e:
    if e.errno != 17:
      raise e

  launcher = _FindPlugin(eclipse_home, 'org.eclipse.equinox.p2.jarprocessor_')
  try:
    files = os.listdir(ziped_update_sites_dir)
  except OSError as e:
    log.error('could not read files in ' + ziped_update_sites_dir)
    raise e

  out = os.path.join(ziped_update_sites_dir, 'out')
  for f in files:
    full_file = os.path.join(ziped_update_sites_dir, f)
    commands = ['java', '-jar', launcher, '-processAll', '-repack',
                '-outputDir', out, full_file]
    print ' '.join(commands)

    subprocess.check_call(commands)

  return out


def PackSite(eclipse_install_dir, ziped_update_sites_dir, eclipse_version):
  """Call the Eclipse Site pack to generate p200 files for the update site.

  Args:
    eclipse_install_dir: directory to unarchive Eclipse if it has not
                          been unarchived yet
    ziped_update_sites_dir: the directory where the zipped update sites
                            are stored
    eclipse_version: the version of eclipse to use for optimizing

  Returns:
    the new directory where the p200 zipfiles were written
  """
  log.debug('packSite({0}, {1}, {2}'.format(eclipse_install_dir,
                                            ziped_update_sites_dir,
                                            eclipse_version))
  eclipse_home = _GetEclipse(eclipse_version, eclipse_install_dir)
  try:
    os.mkdir(eclipse_install_dir)
  except OSError as e:
    if e.errno != 17:
      raise e

  launcher = _FindPlugin(eclipse_home, 'org.eclipse.equinox.p2.jarprocessor_')
  try:
    files = os.listdir(ziped_update_sites_dir)
  except OSError as e:
    log.error('could not read files in ' + ziped_update_sites_dir)
    raise e

  out = os.path.join(ziped_update_sites_dir, 'out')
  for f in files:
    full_file = os.path.join(ziped_update_sites_dir, f)
    commands = ['java', '-jar', launcher, '-processAll', '-pack',
                '-outputDir', out, full_file]
    print ' '.join(commands)
    subprocess.check_call(commands)

  return out


def PublishSite(eclipse_install_dir, update_sites_dir, eclipse_version):
  """Call the Eclipse Site metadata generator for the update site.

  Args:
    eclipse_install_dir: directory to unarchive Eclipse if it has not
                          been unarchived yet
    update_sites_dir: the directory where the update sites
                            are stored
    eclipse_version: the version of eclipse to use for optimizing

  Returns:
    the new directory where the updated zipfiles were written
  """
  log.debug('publishSite({0}, {1}, {2}'.format(eclipse_install_dir,
                                               update_sites_dir,
                                               eclipse_version))
  eclipse_home = _GetEclipse(eclipse_version, eclipse_install_dir)

  launcher = _FindPlugin(eclipse_home, 'org.eclipse.equinox.launcher_')

  out = os.path.join(eclipse_install_dir, 'out')
  for eclipse_ver in ['3.4', '3.5', '3.6', '3.7', '3.8']:
    full_file = os.path.join(update_sites_dir, eclipse_ver)
    if os.path.exists(full_file):
      os.remove(os.path.join(full_file, 'artifacts.jar'))
      os.remove(os.path.join(full_file, 'content.jar'))
      target = 'file:' + full_file
      commands = ['java', '-jar', launcher, '-application',
                  'org.eclipse.equinox.p2.publisher.UpdateSitePublisher',
                  '-metadataRepository', target,
                  '-metadataRepositoryName',
                  'Eclipse ' + eclipse_ver + ' WindowBuilder Repo',
                  '-artifactRepository', target,
                  '-artifactRepositoryName',
                  'Eclipse ' + eclipse_ver + ' WindowBuilder Repo',
                  '-source', full_file,
                  '-compress', '-publishArtifacts']
      print ' '.join(commands)
      subprocess.check_call(commands)

  return out



def RunAnt(eclipse_install_dir, ant_script, prod_dir, eclipse_version):
  """Run an ANt script under Eclipse.

  Args:
    eclipse_install_dir: directory to unarchive Eclipse if it has not
                          been unarchived yet
    ant_script: the ANt Script to run
    prod_dir: the location of the update sites
    eclipse_version: the version of eclipse to use for optimizing
  """
  log.debug('RunAnt({0}, {1}, {2}, {3}'.format(eclipse_install_dir,
                                               ant_script,
                                               prod_dir,
                                               eclipse_version))
  eclipse_home = _GetEclipse(eclipse_version, eclipse_install_dir)
  try:
    os.mkdir(eclipse_install_dir)
  except OSError as e:
    if e.errno != 17:
      raise e

  launcher = _FindPlugin(eclipse_home, 'org.eclipse.equinox.launcher_')
  try:
    files = os.listdir(prod_dir)
  except OSError as e:
    log.error('could not read files in ' + prod_dir)
    raise e

  for f in files:
    full_file = os.path.join(prod_dir, f)
    if os.path.isdir(full_file):
      commands = ['java', '-jar', launcher, '-application', 
                  'org.eclipse.ant.core.antRunner', '-f', 
                  ant_script, '-Dbuild.repo.dir=' + full_file,
                  'process-artifacts']
      log.info(' '.join(commands))
      subprocess.check_call(commands)

def SetArchiveDir(d):
  global eclipse_archive_dir
  eclipse_archive_dir = d

