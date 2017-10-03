define(['angular',
        './controllers/topNavController'],
    function (angular) {
        'use strict';
        return angular.module('meekbiz.topnav', [
            'topnav.controllers.topNav'
        ]);
    });