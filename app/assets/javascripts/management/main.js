define(['angular',
        './controllers/managementController'],
    function (angular) {
        'use strict';
        return angular.module('meekbiz.management', [
            'management.controllers.management'
        ]);
    });