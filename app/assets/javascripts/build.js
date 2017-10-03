/*
 * A custom build profile that is passed to the optimizer via requireJsShim in build.sbt.
 * Play does this via settings it as the mainConfigFile:
 * http://requirejs.org/docs/optimization.html#mainConfigFile
 */
requirejs.config({
    packages: ["common", "topnav", "profile", "index", "miz", "management"],
    paths: {
        // Make the optimizer ignore CDN assets
        "underscorejs": "empty:",
        "jquery": "empty:",
        "angular": "empty:",
        "angular-sanitize": "empty:",
        "jquery-cookie": "empty:",
        "ui-bootstrap": "empty:",
        "ui-bootstrap-tpls": "empty:",
        "jsRoutes": "empty:"
   }
});
