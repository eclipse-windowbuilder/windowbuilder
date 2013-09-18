"""Copyright 2011 Google Inc. All Rights Reserved.

All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html

Contributors:
  Google, Inc. - initial API and implementation

@author: Mark R Russell

Python script to stage a drop of WindowBuilder.
"""
import datetime
import glob
import logging
import logging.config
import optparse
import os
import Queue
import re
import shutil
import subprocess
import sys
from xml.dom import minidom
import eclipse
import signcode
import util


logging.config.fileConfig('logger.config')

log = logging.getLogger('releng')
log.info('starting StageDrop.py')


def main():
  log.debug('in main')
  data = _ProcessArgs()
  drop_location = data['droplocation']
  subproduct = data['subproduct']
  sign_dir = data['signdir']
  eclipse_version = data['eclipseversion']
  optimize_site = data['optimizesite']
  pack_site = data['packsite']
  sign_files = data['signfiles']
  do_deploy = data['dodeploy']
  deploy_dir = data['deploydir']
  dirs2save = data['dirstosave']
  mirrorprod = data['mirrorprod']
  mocksign = data['mocksign']
  base_dir = data['basedir']

  product_dir = os.path.join(base_dir, subproduct)
  log.info('product Dir ' + product_dir)

  if not do_deploy:
    log.info('Initialize ' + sign_dir)

    try:
      os.mkdir(base_dir)
    except OSError as e:
      if e.errno != 17:
        log.error('could not create ' + base_dir)
        raise e

    _RmDirTree(sign_dir)
    _RmDirTree(base_dir)
    os.makedirs(product_dir)
    os.makedirs(sign_dir)

    log.info('Copy files from {0} to {1}'.format(drop_location, product_dir))
    _CopyFiles(drop_location, product_dir, _FilesOnly)

    log.info('Move zip files from {0} to {1}'.format(product_dir, sign_dir))
    _MoveFiles(product_dir, sign_dir, _ZipFilter)

    if sign_files:
      log.info('Sign files')
      if mocksign:
        sc = signcode.SignCode(signcode.MockSign)
      else:
        sc = signcode.SignCode()
      signed_dir = sc.SignZipFiles(sign_dir)
      _CopyFiles(signed_dir, sign_dir, None)
      _RmDirTree(signed_dir)

    if pack_site:
      log.info('pack Site')
      pack_dir = eclipse.PackSite(base_dir, sign_dir, eclipse_version)
      _CopyFiles(pack_dir, sign_dir, None)
      _RmDirTree(pack_dir)

    if optimize_site:
      log.info('Optimize Site')
      optimized_dir = eclipse.OptimizeSite(base_dir, sign_dir, eclipse_version)
      _CopyFiles(optimized_dir, sign_dir, None)
      _RmDirTree(optimized_dir)

    log.info('Move optimized files from {0} to {1}'.format(sign_dir, product_dir))
    _MoveFiles(sign_dir, product_dir, None)
    
    log.info('Unzip the signed files')
    _UnzipSites(product_dir)

    log.info('UpdateMirror')
    _UpdateMirror(product_dir, mirrorprod)

    log.info('Generate Eclipse P2 Metadata')
    eclipse.PublishSite(base_dir, product_dir, eclipse_version)

    log.info('finalProcess')
    eclipse.RunAnt(base_dir, 'finalProcess.xml', product_dir, eclipse_version)

    log.info('Verify Site')
    util._VerifySite(product_dir, sign_files)

    log.info('rezip Site')
    _ReZipSite(product_dir)

    log.info('update MD5 files')
    util.UpdateMd5Hash(product_dir)
  else:
    log.info('doing deployment')
    log.info('deploy code')
    _DeployCode(product_dir, deploy_dir)

  log.info('cleanup')
  _Cleanup(sign_dir, deploy_dir, dirs2save)

  log.debug('done main')


def _ZipFilter(file_in):
  return os.path.isfile(file_in) and file_in.endswith('.zip')


def _ZipOrMd5Filter(file_in):
  return file_in.endswith('.zip') or file_in.endswith('.MD5')


def _FilesOnly(file_in):
  return os.path.isfile(file_in)


def _ProcessArgs():
  """Process the Command Line arguments.

  Returns:
  a dictanary of the arguments values
  """
  sign_dir = os.path.join(os.sep + 'home', 'data', 'httpd',
                          'download-staging.priv', 'tools', 'windowbuilder',
                          'sign')
  deploy_dir = os.path.join(os.sep + 'home', 'data', 'httpd',
                            'download.eclipse.org', 'windowbuilder')
  usage = 'usage: %prog [options] drop subproduct'
  parser = optparse.OptionParser(usage=usage)
  parser.set_defaults(debug=False)
  parser.set_defaults(eclipseversion='4.3')
  parser.set_defaults(eclipsearchivedir='/home/data/httpd/download.eclipse.org'
                      '/eclipse/downloads/drops4')
  parser.set_defaults(optimizesite=True)
  parser.set_defaults(packsite=False)
  parser.set_defaults(signfiles=True)
  parser.set_defaults(dodeploy=False)
  parser.set_defaults(dirstosave='3')
  parser.set_defaults(mirrorprod=False)
  parser.set_defaults(mocksign=False)
  parser.set_defaults(basedir=os.path.join(os.sep + 'shared', 'tools', 'windowbuilder', 'stage'))
  parser.add_option('--signdir', action='store', dest='signdir')
  parser.add_option('-e', '--eclipseversion', action='store',
                    dest='eclipseversion')
  parser.add_option('--eclipsearchivedir', action='store',
                    dest='eclipsearchivedir')
  parser.add_option('--nooptimizesite', action='store_false',
                    dest='optimizesite')
  parser.add_option('--nosignfiles', action='store_false', dest='signfiles')
  parser.add_option('--mocksignfiles', action='store_true', dest='mocksign')
  parser.add_option('--deployfiles', action='store_true', dest='dodeploy')
  parser.add_option('--deploydir', action='store', dest='deploydir')
  parser.add_option('--dirstosave', action='store', dest='dirstosave')
  parser.add_option('--mirrorprod', action='store_true', dest='mirrorprod')
  parser.add_option('--basedir', action='store', dest='basedir')
  (options, args) = parser.parse_args()

  if len(args) != 2:
    parser.error('incorrect number of arguments')

  base_dir = options.basedir
  optimize_site = options.optimizesite
  pack_site = options.packsite
  sign_files = options.signfiles
  do_deploy = options.dodeploy
  mirrorprod = options.mirrorprod
  dirs2save = int(options.dirstosave)

  if options.signdir is not None:
    sign_dir = options.signdir

  if options.deploydir is not None:
    deploy_dir = options.deploydir

  if options.eclipsearchivedir is not None:
    eclipse.SetArchiveDir(options.eclipsearchivedir)

  eclipse_version = options.eclipseversion

  drop_location = args[0]
  subproduct = args[1]

  deploy_dir = os.path.join(deploy_dir, subproduct)

  if drop_location is None:
    log.error('you must specify a drop location')
    parser.error('incorrect number of arguments')
    sys.exit(20)

  if subproduct is None:
    log.error('you must specify a subproduct')
    parser.error('incorrect number of arguments')
    sys.exit(21)

  if do_deploy:
    optimize_site = False
    pack_site = False
    sign_files = False

  ret = dict({'droplocation': drop_location, 'subproduct': subproduct,
              'signdir': sign_dir, 'eclipseversion': eclipse_version,
              'optimizesite': optimize_site, 'packsite': pack_site,
              'signfiles': sign_files, 'dodeploy': do_deploy,
              'deploydir': deploy_dir, 'dirstosave': dirs2save,
              'mirrorprod': mirrorprod, 'mocksign': options.mocksign,
              'basedir': base_dir})
  log.debug('out of processArgs')
  return ret


def _RmDirTree(top):
  """Delere the directory tree.

  Delete everything reachable from the directory named in "top",
  assuming there are no symbolic links.
  CAUTION:  This is dangerous!  For example, if top == '/', it
  could delete all your disk files.

  Args:
    top: the top of te directory tree to delete
  """
  log.debug('rmDirTree(' + top + ')')
  if os.path.exists(top):
    log.info('removing ' + top + ' directory tree')
    if top == '/':
      log.critical('can not pass / as the top directory')
      return
  
    shutil.rmtree(top)


def _CopyFiles(from_dir, to_dir, filt):
  """Copy files using the given filter to select the files to copy.

  Args:
    from_dir: the dorectory to copy the files from
    to_dir: the directory to copy the files to
    filt: the filter to use during copying

  Raises:
    OSError: raised if no files can be read from from_dir
  """
  log.debug('CopyFiles(' + from_dir + ', ' + to_dir)
  try:
    files = os.listdir(from_dir)
  except OSError as e:
    log.error('could not read files in ' + from_dir)
    raise e

  if not files:
    raise OSError('no files to process')

  for f in files:
    full_path = os.path.join(from_dir, f)
    if filt is None or filt(full_path):
      shutil.copy2(full_path, to_dir)


def _MoveFiles(from_dir, to_dir, filt):
  """Move files using the given filter to select the files to move.

  Args:
    from_dir: the dorectory to copy the files from
    to_dir: the directory to copy the files to
    filt: the filter to use during moving

  Raises:
    OSError: raised if no files can be read from from_dir
  """
  log.debug('moveFiles({0}, {1})'.format(from_dir , to_dir))
  try:
    files = os.listdir(from_dir)
  except OSError as e:
    log.error('could not read files in ' + from_dir)
    raise e

  if not files:
    raise OSError('no files to process')

  for f in files:
    full_path = os.path.join(from_dir, f)
    if filt is None or filt(full_path):
      shutil.move(full_path, to_dir)
#
#
#def _SignZipFiles(ziped_update_sites_dir):
#  """Sign the zip files with Eclipses key.
#
#  Args:
#    ziped_update_sites_dir: the directory where the ziped update sites are
#
#  Returns:
#    the directory where the signed jar file are.
#  """
#
#  log.debug('signFiles(' + ziped_update_sites_dir + ')')
#
#  try:
#    files = os.listdir(ziped_update_sites_dir)
#  except OSError as e:
#    log.error('could not read files in ' + ziped_update_sites_dir)
#    raise e
#
#  files_to_sign = []
#  for f in files:
#    if f.endswith('.zip'):
#      zip_path = os.path.join(ziped_update_sites_dir, f)
#      os.chmod(zip_path, stat.S_IWRITE | stat.S_IREAD | stat.S_IWGRP |
#               stat.S_IRGRP | stat.S_IWOTH | stat.S_IROTH)
##      subprocess.check_call(['/bin/echo', 'sign', zip_path, 
##                             'nomail', 'signed'])
#      subprocess.check_call(['/usr/local/bin/sign', zip_path, 'nomail',
#                             'signed'])
#      files_to_sign.append(os.path.join(ziped_update_sites_dir, 'signed', f))
#
#  signed_files = []
#  found = False
#  while not found:
#    found = True
#    for x in range(60):
#      time.sleep(1)
#    for f in files_to_sign:
#      if os.path.exists(f):
#        log.debug(f + ' exists')
#        signed_files.append(f)
#      else:
#        found = False
#        log.debug(f + ' does not exists')
#        continue
#    log.info('all files are not processed yet')
#
#  time.sleep(10)
#  log.info('all files have been signed')
#  return os.path.join(ziped_update_sites_dir, 'signed')


def _UnzipSites(ziped_update_sites_dir):
  log.debug('unzipSites(' + ziped_update_sites_dir + ')')
  try:
    files = os.listdir(ziped_update_sites_dir)
  except OSError as e:
    log.error('could not read files in ' + ziped_update_sites_dir)
    raise e
  version_re = re.compile('.+Eclipse([0-9]\.[0-9]).+')

  for f in files:
    log.debug('processing {0}/{1}'.format(ziped_update_sites_dir, f))
    if f.endswith('.zip'):
      res = version_re.search(f)
      log.debug(util.DisplayMatch(res))
      version = res.group(1)
      unarchive_dest = os.path.join(ziped_update_sites_dir, version)
      if not os.path.exists(unarchive_dest):
        os.mkdir(unarchive_dest)

      util.Unarchive(os.path.join(ziped_update_sites_dir, f), unarchive_dest)


def _ReZipSite(update_site_dir):
  """Rezip the update site.

  Args:
    update_site_dir: the root direcory of the update site
  """
  log.debug('ReZipSite(' + update_site_dir + ')')
  format_dir = 'D: {0:<120} N: {1}'
  format_file = 'F: {0:<120} N: {1}'
  format_zip = '{0:<70} {1:>9} {2:>9}'
  try:
    files = os.listdir(update_site_dir)
  except OSError as e:
    log.error('could not read files in ' + update_site_dir)
    raise e
  version_re = re.compile('.+Eclipse([0-9]\.[0-9]).+')

  for f in files:
    if f.endswith('.zip'):
      zip_file = os.path.join(update_site_dir, f)
      log.info('processing ' + zip_file)
      res = version_re.search(f)
      util.DisplayMatch(res)
      version = res.group(1)
      site_dir = os.path.join(update_site_dir, version)

      log.debug('removing ' + zip_file)
      os.remove(zip_file)
      log.debug('creating zip file ' + zip_file)
      os.chdir(site_dir)
      log.debug('creating zip in ' + os.getcwd())
      command = ['zip', zip_file]

      for root, dirs, files_to_zip in os.walk(site_dir, followlinks=False):
        for name in files_to_zip:
          file_to_zip = os.path.join(root, name)
          zip_file_name = file_to_zip[len(site_dir)+1:]
          log.debug(format_file.format(file_to_zip, zip_file_name))
          if not zip_file_name.endswith('pack.gz'):
            command.append(zip_file_name)

      if log.debug:
        data = 'Command: '
        for cmd in command:
          data = data + cmd + ' '
        log.debug(data)

      subprocess.check_call(command)
    log.debug('out rezipSite')


def _DeployCode(from_dir, to_dir):
  """Deploy the code to the Eclipse Download area.

  Args:
    from_dir: where the code to deploy is located
    to_dir: the place to deploy the code
  """
  log.debug('in deployCode([0], [1])'.format(from_dir, to_dir))
  deploy_dir = to_dir
  try:
    os.mkdir(deploy_dir)
  except OSError as e:
    if e.errno != 17:
      log.error('failed to make directory ' + str(deploy_dir) + ': ' + str(e))
      raise e

  latest_dir = os.path.join(deploy_dir, 'integration')
  d = datetime.datetime.today()
  now_string = d.strftime('%Y%m%d%H%M')
  date_dir = os.path.join(deploy_dir, now_string)
  deploy_dirs = [latest_dir, date_dir]
  log.info('deploying to ')
  log.info(latest_dir)
  log.info('and')
  log.info(date_dir)
  log.debug('out deployCode')
  _RmDirTree(latest_dir)
  for f in deploy_dirs:
    try:
      os.mkdir(f)
    except OSError as e:
      if e.errno != 17:
        log.error('failed to make directory ' + str(f) + ': ' + str(e))
        raise e

    source_files = glob.glob(os.path.join(from_dir, '*'))
    command = ['rsync', '-av']
    for sfile in source_files:
      command.append(sfile)
    command.append(f)

    if log.debug:
      data = 'Command: '
      for cmd in command:
        data = data + cmd + ' '
      log.debug(data)

    subprocess.check_call(command)


def _Cleanup(sign_dir, deploy_dir, dirs_to_save):
  """Cleanup the working directories.

  Args:
    sign_dir: the directory where the signing took place
    deploy_dir: the directory where the deployed code was staged
    dirs_to_save: the number of directories to save
  """
  log.debug('in cleanup(' + sign_dir + ', ' + deploy_dir + ', ' +
            str(dirs_to_save) + ')')
  _RmDirTree(sign_dir)

  pq = Queue.PriorityQueue()
  try:
    for f in os.listdir(deploy_dir):
      if f[0].isdigit(): 
        pq.put(f)
  except OSError:
    log.warn('could not read files in ' + deploy_dir)

  dirs_to_delete = pq.qsize()
  dirs_to_delete -= dirs_to_save
  dir_count = 0
  while not pq.empty():
    d = os.path.join(deploy_dir, pq.get())
    dir_count += 1
    if dir_count <= dirs_to_delete:
      print 'deleting -> ' + d
      _RmDirTree(d)
    else:
      print 'saving  ->  ' + d
  log.debug('out cleanup')


def _UpdateMirror(deploy_dir, mirrorprod):
  """Update the mirror attribute int he site.xml file.

  Args:
    deploy_dir: the directory the code is deployed to
    mirrorprod: flag indicating that the the prod mirror should be used
  """
  log.debug('in updateMirror(' + str(deploy_dir) + ', ' + str(mirrorprod) + ')')
  full_file = os.path.join(deploy_dir, '3.7')
  if os.path.exists(full_file):
    sitefile = os.path.join(full_file, 'site.xml')
    log.debug('processing ' + sitefile)
    dom = minidom.parse(sitefile)
    attr = dom.createAttribute('mirrorsURL')
    site = dom.documentElement
    if mirrorprod:
      attr.value = ('http://www.eclipse.org/downloads/download.php?file='
                    '/windowbuilder/WB/release/R201106211200/3.7&format=xml')
    else:
      attr.value = ('http://www.eclipse.org/downloads/download.php?file='
                    '/windowbuilder/WB/integration/3.7&format=xml')
    site.setAttributeNode(attr)
    f = open(sitefile, 'w')
    site.writexml(f, addindent='   ')
    f.close()

  log.debug('out updateMirror()')


if __name__ == '__main__':
  main()

  log.info('StageDrop.py is done')
