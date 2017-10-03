define(['angular',
        './controllers/indexController'],
    function (angular) {
        'use strict';
        return angular.module('meekbiz.index', [
            'index.controllers.index'
        ]);
    });