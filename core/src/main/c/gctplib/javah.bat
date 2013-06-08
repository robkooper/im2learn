REM For linux, use Makefile as "make javah"

set cpath="C:\Documents and Settings\cykim\workspace\Im2Learn\classes"
set Jpath="C:\Program Files\Java\jdk1.5.0_06\bin\javah"
set iname=ncsa.im2learn.core.geo.projection.MRTInterface

%jpath% -classpath %cpath% -d . %iname%