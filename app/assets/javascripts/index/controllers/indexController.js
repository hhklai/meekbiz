define(['angular', 'common'], function (angular) {
    'use strict';
    var mod = angular.module('index.controllers.index', ['meekbiz.common']);
    mod.controller('IndexCtrl', ['$scope', 'userService',
        function ($scope, userService) {
            $scope.init = function () {
                $scope.isLoggedIn = false;

                userService.checkIfLoggedIn();

                $scope.$watch(function() {
                    return userService.isLoggedIn();
                }, function(isLoggedIn) {
                    $scope.isLoggedIn = isLoggedIn;
                }, true);
            };

            $scope.init();

    }]);
    return mod;
});