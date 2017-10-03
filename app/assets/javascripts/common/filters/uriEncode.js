define(['angular'], function (angular) {
    'use strict';
    var mod = angular.module('common.filters.uriEncode',[]);
    mod.filter('uriEncode', function() {
        return window.encodeURIComponent;
    });
    return mod;
});