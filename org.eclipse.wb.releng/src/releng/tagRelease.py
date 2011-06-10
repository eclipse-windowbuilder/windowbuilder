def processArgs():
  signDir = os.path.join(os.sep + "home", "data", "httpd", 
                         "download-staging.priv", "tools", "windowbuilder")
  deployDir = os.path.join(os.sep + 'home', 'data', 'httpd', 
                           'download.eclipse.org', 'windowbuilder')
  usage = "usage: %prog [options] drop subproduct"
  parser = OptionParser(usage=usage)
  parser.set_defaults(debug=False)
  parser.set_defaults(eclipseversion="3.7")
  parser.set_defaults(optimizesite=True)
  parser.set_defaults(packsite=True)
  parser.set_defaults(signfiles=True)
  parser.set_defaults(dodeploy=False)
  parser.set_defaults(dirstosave="7")
  parser.add_option("--signdir", action="store", dest="signdir")
  parser.add_option("-e", "--eclipseversion", action="store", 
                    dest="eclipseversion")
  parser.add_option("--eclipsearchivedir", action="store", 
                    dest="eclipsearchivedir")
  parser.add_option("--nooptimizesite", action="store_false", dest="optimizesite");
  parser.add_option("--nopacksite", action="store_false", dest="packsite")
  parser.add_option("--nosignfiles", action="store_false", dest="signfiles")
  parser.add_option("--deployfiles", action="store_true", dest="dodeploy")
  parser.add_option("--deploydir", action="store", dest="deploydir")
  parser.add_option("--dirstosave", action="store", dest="dirstosave")
  (options, args) = parser.parse_args()
  
  if len(args) != 2:
    parser.error("incorrect number of arguments")
    
  optimizeSite = options.optimizesite
  packSite = options.packsite
  signFiles = options.signfiles
  doDeploy = options.dodeploy
  dirs2save = int(options.dirstosave)


  if options.signdir != None:
    signDir = options.signdir

  if options.deploydir != None:
    deployDir = options.deploydir
  
  if options.eclipsearchivedir != None:
    eclipse.setArchiveDir(options.eclipsearchivedir)
    
  eclipseVersion = options.eclipseversion
       
  dropLocation = args[0]
  subproduct = args[1]

  deployDir = os.path.join(deployDir, subproduct);
  
  if dropLocation == None:
    log.error("you must specify a drop location")
    usage()
    sys.exit(20)

  if subproduct == None:
    log.error("you must specify a subproduct")
    usage()
    sys.exit(21)
    
  if doDeploy:
    optimizeSite = False
    packSite = False
    signFiles = False
    
  
  ret = dict({'droplocation':dropLocation, 'subproduct':subproduct, 
              'signdir':signDir, 'eclipseversion':eclipseVersion,
              'optimizesite':optimizeSite, 'packsite':packSite,
              'signfiles':signFiles, 'dodeploy':doDeploy,
              'deploydir':deployDir, 'dirstosave':dirs2save})
  log.debug("out of processArgs")
  return ret
