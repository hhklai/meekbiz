(function (requirejs) {
    'use strict';

    var tests = [];
    for (var file in window.__karma__.files) {
        if (window.__karma__.files.hasOwnProperty(file)) {
            if (/Spec\.js$/.test(file)) {
                tests.push(file);
            }
        }
    }

    // -- test RequireJS config --
    requirejs.config({
        packages: ['common', 'topnav', 'profile', 'index', 'miz', 'management'],
        baseUrl: '/base/app/assets/javascripts',
        shim: {
            'underscorejs': {deps: [], exports: '_'},
            'jquery': {deps: [], exports: 'jquery'},
            'angular': {deps: [], exports: 'angular'},
            'angular-sanitize': {deps: ['angular'], exports: 'angular-sanitize'},
            'angular-mock': {deps: ['angular'], exports: 'angular-mock'},
            'jquery-cookie': {deps: [], exports: 'jquery-cookie'},
            'ui-bootstrap': {deps: [], exports: 'ui-bootstrap'},
            'ui-bootstrap-tpls': {deps: [], exports: 'ui-bootstrap-tpls'},
            'jsRoutes': {deps: [], exports: 'jsRoutes'}
        },
        paths: {
            'underscorejs': '../../../test/angular/include/underscore-1.8.3.min',
            'jquery': '../../../test/angular/include/jquery-2.1.1.min',
            'angular': '../../../test/angular/include/angular-1.3.0-rc.1.min',
            'angular-sanitize': '../../../test/angular/include/angular-sanitize-1.3.0-beta.18.min',
            'angular-mock': '../../../test/angular/include/angular-mocks-1.3.0-rc.1',
            'jquery-cookie': '../../../test/angular/include/jquery.cookie-1.4.0.min',
            'ui-bootstrap': '../../../test/angular/include/ui-bootstrap-0.11.0',
            'ui-bootstrap-tpls': '../../../test/angular/include/ui-bootstrap-tpls-0.11.0',
            'jsRoutes': '../../../test/angular/include/emptyFile'
        },
        deps: tests,
        callback: window.__karma__.start
    });

    requirejs.onError = function (err) {
        console.log(err);
    };
})(requirejs);
