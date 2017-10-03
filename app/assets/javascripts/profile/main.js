define(['angular',
        './controllers/profileController',
        './controllers/userSummaryController'],
    function (angular) {
        'use strict';
        return angular.module('meekbiz.profile', [
            'profile.controllers.profile',
            'profile.controllers.profileUserSummary'
        ]);
    });