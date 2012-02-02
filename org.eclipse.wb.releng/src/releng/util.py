"""Copyright 2011 Google Inc. All Rights Reserved.

All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html

Contributors:
  Google, Inc. - initial API and implementation

@author: Mark R Russell
"""


import logging
import os
import shutil
import subprocess
import tempfile

log = logging.getLogger('releng.util')


def Unarchive(archive, dest):
  """Unarchive the given archive to the given destination.

  Args:
    archive: the archive to process
    dest: the destination to unarchive the given file
  Raises:
    Exception: if there was an error unarchiving the archive
  """
  log.debug('Unarchive({0}, {1})'.format(archive, dest))
  cwd = os.getcwd()
  os.chdir(dest)
  commands = []
  if archive.endswith('tar.gz') or archive.endswith('tgz'):
    commands.append('/bin/tar')
    commands.append('xzvf')
    commands.append(archive)
  elif archive.endswith('tar'):
    commands.append('/bin/tar')
    commands.append('xvf')
    commands.append(archive)
  elif archive.endswith('zip'):
    commands.append('/usr/bin/unzip')
    commands.append(archive)
  try:
    log.warning(' '.join(commands))
    
    status = subprocess.call(commands)
    if status:
      msg = ('unarchive failed for {0}: '
             'return code was {1}').format(archive, status)
      log.error(msg)
      raise Exception(msg)

  finally:
    os.chdir(cwd)


def UpdateMd5Hash(directory):
  """Update the md5 hashs for the Zip files in the given directory.

  Args:
    directory: the directory where the files are the need the md5 has updated
  """
  log.debug('updateMd5Hash({0})'.format(directory))
  try:
    files = os.listdir(directory)
  except OSError as e:
    log.error('could not read files in ' + directory)
    raise e

  for f in files:
    if f.endswith('.zip'):
      full_file = os.path.join(directory, f)
      commands = ['/usr/bin/md5sum', '-b', full_file]
      if log.isEnabledFor(log.info):
        log.info('command line: {0}'.format(' '.join(commands)))

      md5_file = os.path.join(directory, f + '.MD5')
      fd_md5 = open(md5_file, 'w')
      log.info('creating ' + md5_file)
      subprocess.check_call(commands, stdout=fd_md5)


def _VerifySite(directory, sign_files):
  """Verify the site is packed and signed correctly.

  Args:
    directory: the directory where the files to verify are located
    sign_files: flag indicating if the files are signed
  """
  log.debug('_VerifySite({0}, {1})'.format(directory, sign_files))
  elements = os.listdir(directory)
  packed_files = []
  for e in elements:
    full_path = os.path.join(directory, e)
    if os.path.isdir(full_path):
      for root, dirs, files in os.walk(full_path):
        log.debug('Current directory {0}'.format(root))
        for f in files:
          if str(f).endswith('.pack.gz'):
            packed_files.append(os.path.join(root, f))
  
  current_dir = os.getcwd()
  working_dir = tempfile.mkdtemp(prefix='verify-')
  processing_file = None
  error_list = []
  try:
    os.chdir(working_dir)
    packed_files.sort()
    for f in packed_files:
      log.debug('processing: {0}'.format(f))
      f_jar = str(f)[0:f.rindex('.pack.gz')]
      short_jar = os.path.basename(f_jar)
      commands = ['unpack200', f, os.path.abspath(os.path.join('.', short_jar))]
      if log.isEnabledFor(log.info):
        log.info('command line: {0}'.format(' '.join(commands)))
      subprocess.check_call(commands)
      elements = os.listdir(working_dir)
      for e in elements:
        processing_file = e
        if sign_files:
          commands = ['jarsigner', '-verify', e]
          if log.isEnabledFor(log.info):
            log.info('command line: {0}'.format(' '.join(commands)))
          p = subprocess.Popen(commands, stderr=subprocess.PIPE,
                               stdout=subprocess.PIPE)
          (stdout, stderr) = p.communicate()
          if str(stdout).find('jar verified') < 0:
            msg = 'failed to validate {0}'.format(processing_file)
            error_list.append(msg)
            log.error(msg)
        else:
          log.warning('would do: jarsigner -verify {0}'.format(e))
        os.remove(e)
  finally:
    os.chdir(current_dir)
    shutil.rmtree(working_dir)

  if error_list:
    log.error("************************************************************")
    log.error("verify errors")
    log.error("************************************************************")
    log.error("************************************************************")
    for error_line in error_list:
      log.error(error_line)
    log.error("************************************************************")
    log.error("************************************************************")
    log.error("************************************************************")
    

def DisplayMatch(match):
  if match is None:
    return None
  return '<Match: %r, groups=%r>' % (match.group(), match.groups())
