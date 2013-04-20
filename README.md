# PannelLayout

An Android library for arranging views in panels, Duh. 
 
## Summary

This is an attempt to create a view group that can layout view in a grid like the Google Play app does. It allows the developer to specify a row and column span for each child and have the view group figure out where to place them. Each panel is constrained so that it cannot grow outside of the row and column spans specified in it's layout parameters.

Yes I spelled "panel" wrong but, now it's in the name and, it's stuck that way. 

## Licence

MALForAndroid 2 is licenced under: 

Apache License Version 2.0, January 2004

## Usage Example

	<com.github.riotopsys.pannellayout.PannelLayout xmlns:android="http://schemas.android.com/apk/res/android"
    	xmlns:tools="http://schemas.android.com/tools"
	    xmlns:app="http://schemas.android.com/apk/res-auto"
    	android:layout_width="fill_parent"
	    android:layout_height="wrap_content"
    	android:background="#000"
	    app:columns="3"
	    app:divider_size="4dp" >

    	<View
        	android:layout_width="fill_parent"
	        android:layout_height="fill_parent"
    	    android:background="#0F0"
        	app:column_span="1" />

	    <View
    	    android:layout_width="fill_parent"
        	android:layout_height="fill_parent"
	        android:background="#0FF"
    	    app:row_span="2" />

	    <View
    	    android:layout_width="fill_parent"
        	android:layout_height="fill_parent"
	        android:background="#00F" />

    	<View
        	android:layout_width="fill_parent"
	        android:layout_height="fill_parent"
    	    android:background="#F0F"
        	app:column_span="1"
	        app:row_span="1" />

	</com.github.riotopsys.pannellayout.PannelLayout>

## Contributing

If you want to contribute to PannelLayout, just fork and submit a pull request!

## Changelog

This is still in very early development.
