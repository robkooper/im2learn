-------------------------------------------------------------------------------
--                             WHAT IS THIS                                  --
-------------------------------------------------------------------------------

This is a complete rewrite of I2K, you should probably not be using it. The
ncsa.im2learn.main idea is to make the whole more object oriented. In core will
be the core functions of Im2Learn, these will be very stable and hardly change.
In ext will be all the extra code that is developed over time.

-------------------------------------------------------------------------------
--                            DOCUMENTATION                                  --
-------------------------------------------------------------------------------

This version of Im2Learn is split into seperate parts, the first part is the
core section. In here all the core functions for Im2Learn live. Most of the code that is
in this package will be included in every build. The second section is the ext
section. In here will be any additions that are made to Im2Learn. Some of this code
will be general, some of it will be for specific projects. Finally there is the
ncsa.im2learn.main package. In here will be the ncsa.im2learn.main classes for each deliverable.

ncsa.im2learn.main package
------------

The ncsa.im2learn.main package contains the entry points for all deliverables. The Minimal
class contains the minimal version of Im2Learn. All this will do is show the Im2Learn
frame, the open/save menus and the help menu. It also has the ability to zoom
and crop.

The Im2LearnNCSA class contains the NCSA version of Im2Learn. This is the version that
is used internally in NCSA. All menu options are added to this version and all
capabilities are enabled.

core package
------------

The core package contains 3 subpackages, datatype, gui and io. The datatype
package will contain all the basic datatypes used in Im2Learn. The gui package will
contain all the basic gui components that can be used, and io contains all the
classes that deal with loading and saving of imagedata.

datatype package

In the datatype package are all the core datatypes used in Im2Learn. Most important
in this package is ImageObject (and the subclass for each type). ImageObject
is a wrapper around an array to hold the imagedata and accessor functions for
the data (for each primitive datatype). The accessor functions are as simple
as possible and will add very little overhead. ImageObject also contains a set
of properties. The user can use this to add any other information that belongs
to the ImageObject

The subarea package is an extention of Rectangle with additional support.

gui package

The ncsa.im2learn.main classes in the gui package are ImageComponent, which will render the
ImageObject onto a JComponent, and ImagePanel which adds capabilites for
markers, selection and popup menu to ImageComponent. ImageComponent has the
capabilites to select which bands to show (including grayscale), change the
gamma, crop and zoom factor.

Im2LearnMainFrame is the abstract class that all ncsa.im2learn.main classes will extend. It will
setup the basic Im2Learn frame with the basic menu entries. The ncsa.im2learn.main class will need
to implement the function that returns the customer name.

FeedbackThread is a simple class that shows a modal dialog preventing all user
interaction while a new thread is created to execute the user code. Once the
usercode is done, the modal dialog will disappear allowing the user to interact
with Im2Learn again.

-------------------------------------------------------------------------------
--                            SPEED ISSUES                                   --
-------------------------------------------------------------------------------

The generic method of accessing the data is slowest (about 4 times). You can
speed things up by adding a simple switch statement to typecast the generic
object to the correct type object. Having seperate loops for each type will
speed this even more up. Fastest is to do a getData() typecast the result to
the correct type and access the array directly. However this is also the most
code and most errorprone, while generic methods is the least code and less
errorprone. Best is to use the generics while developping code, and switch to
specifics when optimizing the code.

loop size is           : 7500000
Testing direct         : 151.6
Testing function       : 181.2 slower = 1.1952506596306067
Testing image type     : 204.7 slower = 1.3502638522427441
Testing image check    : 225.0 slower = 1.4841688654353562
Testing image generic  : 435.9 slower = 2.87532981530343

As you can see doing a simple check inside the loop, what imagetype it is and
typecasting the ImageObject to this this type will speed things up. The reason
for the slowdown, is the fact that the abstract class will need to do a runtime
check to see what class it really is and call the right function.

So instead of using:
      ImageObject imageobject;
      for(i=0; i<size; i++) {
         imageobject.set(i, 0);
      }

use:

    ImageObject imageobject;
    for(i=0; i<size; i++) {
        switch (imageobject.getType()) {
            case ImageObject.TYPE_BYTE:
                ((ImageObjectByte)imageobject).set(i, 0);
                break;
            case ImageObject.TYPE_SHORT:
                ((ImageObjectShort)imageobject).set(i, 0);
                break;
            case ImageObject.TYPE_INT:
                ((ImageObjectInt)imageobject).set(i, 0);
                break;
            case ImageObject.TYPE_LONG:
                ((ImageObjectLong)imageobject).set(i, 0);
                break;
            case ImageObject.TYPE_FLOAT:
                ((ImageObjectFloat)imageobject).set(i, 0);
                break;
            case ImageObject.TYPE_DOUBLE:
                ((ImageObjectDouble)imageobject).set(i, 0);
                break;
        }
    }

of course better is:

    switch (imageobject.getType()) {
        case ImageObject.TYPE_BYTE:
            ImageObjectByte imgbyte = (ImageObjectByte)imageobject;
            for(i=0; i<size; i++) {
                imgbyte.set(i, 0);
            }
            break;
        ....
        case ImageObject.TYPE_DOUBLE:
            ImageObjectDouble imgdouble = (ImageObjectDouble)imageobject;
            for(i=0; i<size; i++) {
                imgdouble.set(i, 0);
            }
            break;
    }

finally nothing beats:
    switch (imageobject.getType()) {
        case ImageObject.TYPE_BYTE:
            java.util.Arrays.fill((byte[])(imageobject.getData()), (byte)0);
            break;
        ....
        case ImageObject.TYPE_DOUBLE:
            java.util.Arrays.fill((double[])(imageobject.getData()), (double)0);
            break;
    }

 Basicly what I am trying to say is, code smart and the speed will follow. If
 possible use built-in java code like System.arraycopy (which is a single
 assembly instruction on a x86).

-------------------------------------------------------------------------------
--                             GUIDELINES                                    --
-------------------------------------------------------------------------------

For a description of javadoc and the tags see:
http://java.sun.com/j2se/javadoc/writingdoccomments/

- Any images used by the classfile are placed in the source tree with the class
  that uses the images. You can access the images using
  classname.class.getResource("image.gif"); this will return a URL.
- All functions should have a javadoc explaining what that function does.
- All variables should be private or protected and have simple getters/setters.
- All classes that show a dialog or add menus, will end with Dialog.
- Should write some documentation about what this class/package does in the docs
  directory.
- Images are in the source tree in a directory called doc-files, you reference
  them in the javadoc simply with <img src="doc-files/class-x.jpg">
- Each subdirectory should have a package.html file, you can use the following
  as a starting point:

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2 Final//EN">
<html>
<head>
</head>
<body bgcolor="white">

Oneliner about package. Bigger description about package. This will be used
when generating javadocs.

@since 2.0

</body>
</html>
