Image Cache
===========

An image download-and-cacher that also knows how to efficiently generate
thumbnails of various sizes. 

Features
--------

* Easily integrates into content-provider backed applications, providing an
  Adapter that can read local and web URLs from a Cursor
* Automatic generation and caching of multiple sizes of images based on one
  downloaded asset
* Provides a disk cache as well as a memory cache
* Designed to work with your existing setup: no extending a custom Application
  or Activity needed
* Cursor Adapter Supports multiple image fields for each ImageView; skips
  fields that are null or empty
* Cursor Adapter has an automatic progress bar when loading the cursor

License
=======

MEL Android Utils  
Copyright (C) 2011 [MIT Mobile Experience Lab][mel]

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

[mel]: http://mobile.mit.edu/
