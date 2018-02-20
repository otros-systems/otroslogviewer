# OtrosLogViewer
Useful software for analysing applications logs and traces.

![Build status](https://api.travis-ci.org/otros-systems/otroslogviewer.svg?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8a6c121e140b4493911e064621e0e635)](https://www.codacy.com/app/Otros-systems/otroslogviewer)
[![Download OtrosLogViewer](https://img.shields.io/sourceforge/dm/otroslogviewer.svg)](https://sourceforge.net/projects/otroslogviewer/files/latest/download)
[![Download OtrosLogViewer](https://img.shields.io/sourceforge/dt/otroslogviewer.svg)](https://sourceforge.net/projects/otroslogviewer/files/latest/download)


## Requirements
  * Java 8 since version 1.4.0
  * Java 6 for verions 1.3.x

## Download 
  * You can download latest version from [Sourceforge page](https://sourceforge.net/projects/otroslogviewer/files/latest/download?source=directory) 
  

## Features
  * Loading logs from remote servers using ftp, sftp, ssh, samba and others ([supported file systems](http://commons.apache.org/vfs/filesystems.html))
  * Tailing logs from local disk and sftp
  * Parsing logs from clipboard
  * Decompressing "gziped" logs on the fly
  * Parsing custom log patterns
  * Log events searching using regular expression
  * Log filters
  * Pluginable log filters
  * Log highlightings
  * Automatic log highlightings based on string match, regular expression or custom Java code
  * Pluginable log details formatters and colorizers (i.e. SOAP message)
  * Pluginable log highlightings
  * Pluginable log parsers
  * Listening on a socket
  * Adding notes to log event
  * Saving/loading log investigation (with added marks and notes)
  * Integration with [IntelliJ IDEA](https://github.com/otros-systems/otroslogviewer/wiki/JumpToCode) and [Eclipse](https://github.com/otros-systems/otroslogviewer/wiki/JumpToCode)
  * Integration with stackoverflow.com


## Included parsers
  * Java Util Logging XMLFormatter
  * Java Util Logging SimpleFormatter
  * log4j pattern layout [(how to create your own)](https://github.com/otros-systems/otroslogviewer/wiki/Log4jPatternLayout)
  * [JSON format](https://github.com/otros-systems/otroslogviewer/wiki/Parsing-logs-in-JSON-format)
  * log4j XMLLayout

## Included filters
  * String
  * Regular expression
  * Level
  * Time
  * Highlighted
  * Class
  * Thread
  * Highlighted
  * With note, marked


## Screenshots

Filter level WARNING or higher and highlighting:

![http://lh5.ggpht.com/_qGxhLPhk4wE/TTixTV-fyNI/AAAAAAAAA18/ILCMZ_siOIE/olv-2011-01-20.png](http://lh5.ggpht.com/_qGxhLPhk4wE/TTixTV-fyNI/AAAAAAAAA18/ILCMZ_siOIE/olv-2011-01-20.png)

[Click here to see more screenshots](https://github.com/otros-systems/otroslogviewer/wiki/Screenshots)

## Video
[Screen cap recording ](https://github.com/otros-systems/otroslogviewer/wiki/Wideo)

## Powered by
  * Apache commons: net, lang, vfs, httpclient, logging, collections, compress, io
  * log4j
  * VFSJFileChooser
  * Jakarta ORO
  * jCIFS
  * jSch
  * [fugue icons](http://code.google.com/p/fugue-icons-src/)
  * MigLayout
  * Trident animation library
  * JGoodies Looks

## Similar software
  * [log.io](http://logio.org/)
  * [Apache chainsaw](http://logging.apache.org/chainsaw/index.html)
  * [Lilith](http://lilith.huxhorn.de/)
  * [eclipse logviewer](http://code.google.com/a/eclipselabs.org/p/logviewer/)
  * [Vigilog](http://vigilog.sourceforge.net/index.html)
  * [JLogViewer](http://sourceforge.net/projects/jlogviewer/)

## Contact
If you have any questions please write to otros.systems at gmail.com

Follow us on [Twitter](https://twitter.com/OtrosSystems)
Created with: [![](http://www.jetbrains.com/idea/opensource/img/all/banners/idea125x37_white.gif)](http://www.jetbrains.com/idea/)


## Do you want help
Do you want to help? You can donate to OtrosLogViewer

Using **PayPal**:
[![](https://www.paypalobjects.com/webstatic/mktg/logo-center/PP_Acceptance_Marks_for_LogoCenter_76x48.png)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=GJUQP3X5FMUQU&lc=US&item_name=OtrosLogViewer%20%2d%20donate&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)

Using **BitCoin**: 1JYDZMysFaXv1u58susqEqX4ukdtSnLLpW

[![](http://petition.stopsoftwarepatents.eu/banner/171006582880/ssp-362-60.gif)](http://petition.stopsoftwarepatents.eu/171006582880/)


