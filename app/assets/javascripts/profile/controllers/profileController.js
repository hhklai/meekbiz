define(['angular', 'underscorejs', 'common'], function (angular, underscorejs) {
    'use strict';
    var mod = angular.module('profile.controllers.profile', ['meekbiz.common']);
    mod.controller('ProfileCtrl', ['$scope', 'userService', 'mizService', 'playRoutes', '$window', '$q',
        function ($scope, userService, mizService, playRoutes, $window, $q) {
        var hasInitLoggedOutWatch = false;

        $scope.init = function () {
            $scope.pendingMizes = [];
            if (!$window.readOnly) {
                userService.getPrivateInfo().then(function(user) {
                    $scope.user = user;
                    $scope.detectLogout();
                },
                function () {
                    $window.location.href = '/';  //redirect to index
                });

                $scope.routes = playRoutes.view.controllers; //view routes
                mizService.getPendingMizes().then(function (mizes) {
                    $scope.pendingMizes = mizes;
                });
            }

            angular.element(window).on('beforeunload', function () {
                if ($scope.isEditting) {
                    return 'You are currently editing a form.';
                }
            });
            
            $scope.readOnly =  !!$window.readOnly;
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

        $scope.getThumbnailLocation = function (miz) {
            if (miz.thumbnail ==  null) {
                return "/assets/img/miz/round_question_mark.jpg";
            } else {
                return meekbiz.settings.s3host + miz.thumbnail + ".jpg"
            }
        };

        $scope.deleteMiz = function (userName, mizTitle) {
            var deferred = $q.defer();
            var scope = $scope;
            mizService.deleteMiz(userName, mizTitle).then(function () {
                var deleteInd = underscorejs.findIndex(scope.pendingMizes, {title: mizTitle});
                scope.pendingMizes.splice(deleteInd, 1);
                deferred.resolve();
            }, function () {
                deferred.reject();
            });
            return deferred.promise;
        };

        $scope.init();
    }]);
    return mod;
});