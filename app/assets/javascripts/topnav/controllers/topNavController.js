define(['angular', 'common'], function (angular) {
    'use strict';
    var mod = angular.module('topnav.controllers.topNav', ['meekbiz.common']);
    mod.controller('TopNavCtrl', ['$scope', 'userService', function ($scope, userService) {
        $scope.init = function () {
            userService.checkIfLoggedIn();

            $scope.$watch(function() {
                return userService.isLoggedIn();
            }, function(isLoggedIn) {
                $scope.isLoggedIn = isLoggedIn;
            }, true);
        };

        $scope.logout = function() {
            userService.logout();
            $scope.isLoggedIn = undefined;
        };

        $scope.init();
    }]);
    return mod;
});