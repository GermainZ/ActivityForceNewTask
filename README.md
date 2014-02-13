ActivityForceNewTask
====================
Apps can launch other apps' activities inside their task. This makes it
impossible to switch back and forth between the two. This module sets the
FLAG_ACTIVITY_NEW_TASK flag when starting a new activity, which 'fixes' that
(note to devs: only startActivity is hooked for obvious reasons.)
Note that this module may break things. See the XDA thread for more info.

Download Module
===============
http://repo.xposed.info/module/com.germainz.activityforcenewtask

XDA Thread
==========
http://forum.xda-developers.com/showthread.php?t=2646504
