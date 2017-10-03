define(['angular', 'common'], function (angular) {
    'use strict';
    var mod = angular.module('miz.controllers.miz', ['meekbiz.common']);
    mod.controller('MizCtrl', ['$scope', 'userService', 'mizService', '$window',
        function ($scope, userService, mizService, $window) {
            var hasInitLoggedOutWatch = false;

            $scope.init = function () {
                $scope.readOnly =  !!$window.readOnly;
                if (!$scope.readOnly) {
                    mizService.getPrivateMiz(window.username, window.mizTitle).then(
                        function (miz) {
                            $scope.miz = miz;
                            $scope.detectLogout();
                        },
                        function () {
                            $window.location.href = '/';  //redirect to index
                        }
                    );
                }
            };

            $scope.detectLogout = function () {
                if (!hasInitLoggedOutWatch) {
                    $scope.$watch(function() {
                        return userService.isLoggedIn();
                    }, function(isLoggedIn) {
                        if (!isLoggedIn) {
                            $window.location.href = '/';
                        }
                    }, true);
                }
                hasInitLoggedOutWatch = true;
            };

            $scope.init();

    }]);
    return mod;
});