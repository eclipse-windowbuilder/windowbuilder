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
import subprocess

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


def DisplayMatch(match):
  if match is None:
    return None
  return '<Match: %r, groups=%r>' % (match.group(), match.groups())
