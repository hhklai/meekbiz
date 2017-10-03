(function (requirejs) {
    "use strict";

    // -- DEV RequireJS config --
    requirejs.config({
        // Packages = top-level folders; loads a contained file named "main.js"
        packages: ["common", "topnav", "profile", "index", "miz", "management"],
        shim: {
            'jsRoutes': {deps: [], exports: 'jsRoutes'}
        },
        paths: {
            "jsRoutes": "/jsroutes",
            //items that should be in webjar shims but isn't
            "jquery-cookie": webjars.path("jquery-cookie", "jquery.cookie")
        }
    });

    requirejs.onError = function (err) {
        console.log(err);
    };

    // Load the app.
    require(["angular", "jquery", "topnav", "profile", "index", "miz", "management"],
        function (angular, jQuery) {
            angular.bootstrap(document, ["meekbiz.topnav", "meekbiz.profile", "meekbiz.index", "meekbiz.miz", "meekbiz.management"]);

            //prevent default behavior for dragging things on the page
            jQuery('body')
                .bind( 'dragenter dragover', false)
                .bind( 'drop', function( e ) {
                    e.stopPropagation();
                    e.preventDefault();
                });
        }
    );
})(requirejs);
