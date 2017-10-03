define(['angular', 'jquery-cookie'], function(angular) {
    'use strict';
    var mod = angular.module('common.lib.cookie', []);
    mod.factory('cookieFactory', function () {
        return {
            getCookie: function (name) {
                return $.cookie(name);
            },

            getAllCookies: function () {
                return $.cookie();
            },

            setCookie: function (name, value, options) {
                return $.cookie(name, value, options);
            },

            deleteCookie: function (name) {
                return $.removeCookie(name);
            }
        }
    });
    return mod;
});