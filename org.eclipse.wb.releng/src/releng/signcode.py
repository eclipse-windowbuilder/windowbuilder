'''
Created on Feb 2, 2012

@author: mrrussell
'''
import logging
import os
import subprocess
import time
import stat
import tempfile
import shutil

log = logging.getLogger('releng.signcode')

class SignCode(object):
  '''Class to sign Eclipse update site zips.
  '''
  _sign_method = None


  def __init__(self, signing_method=None):
    """
        Constructor
    """
    if signing_method is None:
      self._sign_method = self._EclipseSign 
    else:
      self._sign_method = signing_method

  def SignZipFiles(self, files_location, selection_method = None):
    """Sign the zip files with Eclipses key.

      The signed files will be in files_location/signed.

    Args:
      files_location: the directory where the ziped update sites are

    Returns:
      the directory where the signed jar file are.
    """

    log.debug('signFiles({0})'.format(files_location))

    signed_location = os.path.join(files_location, 'signed')
    os.makedirs(signed_location)
    signed_files = self._sign_method(self._CollectFiles(files_location,
                                                        selection_method), 
                                     signed_location)
    return signed_location
    
  def _CollectFiles(self, files_location, selection_method=None):
    """Collect the files to process.
    Args:
      files_location: the location of the files to process
      selection_method: the method to select the files
    Returns:
      a list of files to process
    Exception:
      OSError if files_location can not be read
    """
    if selection_method is None:
      selection_method = self._SelectZip
    log.debug('_CollectFiles({0}, selection_method)'.format(files_location))
    try:
      files = os.listdir(files_location)
    except OSError as e:
      log.error('could not read files in ' + files_location)
      raise e

    files_to_sign = []
    for f in files:
      full_file = os.path.join(files_location, f)
      if selection_method(full_file):
        os.chmod(full_file, stat.S_IWRITE | stat.S_IREAD | stat.S_IWGRP |
                 stat.S_IRGRP | stat.S_IWOTH | stat.S_IROTH)
        files_to_sign.append(full_file)
    return files_to_sign

  def _SelectZip(self, file_name):
    """Select a file if it ends with '.zip'
    Args:
      file_name: the name of the file to process
    Returns:
      True if the file_name is a file and it ends in '.zip', otherwise False
    """
    isfile = os.path.isfile(file_name)
    endswithzip = file_name.endswith('.zip')
    return isfile and endswithzip

  def _EclipseSign(self, files_to_sign, sign_location):
    """
    """
    signed_files = []
    log.debug('_EclipseSign({0}, {1}'.format(files_to_sign, sign_location))
    if files_to_sign:
      for zip_path in files_to_sign:
        cmd = ['/usr/local/bin/sign', zip_path, 'now', 'signed']
        if log.debug:
          log.debug(' '.join(cmd))
        subprocess.check_call(cmd)
  
      log.info('all files have been signed')
    else:
      log.warning('no files to process')
    return signed_files

def SelfSign(self, files_to_sign, sign_location):
  """
  """
  base_dir = tempfile.mkdtemp(prefix='unzip-site')
  try:
    
    for zip_file in files_to_sign:
      subprocess.check_call('unzip', )
  finally:
    shutil.rmtree(base_dir)
  return files_to_sign

def MockSign(self, files_to_sign, sign_location):
  """
  """
  for zip_file in files_to_sign:
    shutil.copy2(zip_file, sign_location)
  return files_to_sign
