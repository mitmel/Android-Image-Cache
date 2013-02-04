Image Cache
===========

An image download-and-cacher that also knows how to efficiently generate
and retrieve thumbnails of various sizes.

Features
--------

* easily integrates into content-provider backed applications, providing an
  adapter that can read local and web URLs from a cursor
* automatic generation and caching of multiple sizes of images based on one
  downloaded asset
* provides a disk cache as well as a memory cache
* automatic disk cache management; no setup necessary, but parameters can be
  fine-tuned if desired
* designed to work with your existing setup: no extending a custom application
  or activity needed
* cursor adapter supports multiple image fields for each ImageView; skips
  fields that are null or empty
* cursor adapter has an automatic progress bar when loading the cursor

Using
-----

Please see the `test/` directory for both a simple example of using it as well as
some unit tests. When running the application in `test/` make sure to run it as
an Android activity if you want to see the demo.

Both the unit tests and the interactive test load some images from our lab's servers.

License
=======

MEL Android Image Cache  
Copyright (C) 2011-2013 [MIT Mobile Experience Lab][mel]

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License version 2.1 as published by the Free Software Foundation.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

[mel]: http://mobile.mit.edu/
