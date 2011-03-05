TODO: reverse-proxy selenium from Jenkins for easier configuration

TODO: launch multiple RCs per node
TODO: environment configurations
  patch Selenium Grid to have multiple environments per node, and let the user configure them via labels,
  or define NodeProperty.
TODO: 

-----------

A single selenium RC instance is capable of starting any browser --- it does so by using the browser string
given per session. But this is bit problematic with Slenium Grid, which wants to pick the right node
based on the browser (so say someone said "*safari" and you don't want a Windows node to pick that up.)

So it does this by adding a translation layer.

Problems:

 - one RC can only register one environment, but that's silly.

----
If we replace GlobalRemoteControlPool with our own implementation, we can use labels for environments,
but doing so requires patching HubRegistry (or doing reflection access.)
