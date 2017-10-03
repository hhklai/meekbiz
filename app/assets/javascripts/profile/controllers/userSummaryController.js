define(['angular', 'common'], function (angular) {
    'use strict';
    var mod = angular.module('profile.controllers.profileUserSummary', ['meekbiz.common']);
    mod.controller('ProfileUserSummaryCtrl', ['$scope', 's3fileService', 'userService', '$window', '$q',
        function ($scope, s3fileService, userService, $window, $q) {

        $scope.init = function () {
            if (!$window.readOnly) {
                $scope.isEditting = false;
                $scope.isLoading = false;
                $scope.photoHasChanged = false;
                $scope.thumbnail = '';
                $scope.user = {};
                $scope.$watch(function() {
                    return $scope.$parent.user;
                }, function(user) {
                    if (user && !$scope.form.$dirty && !$scope.photoHasChanged) {
                        $scope.user.name = user.name;
                        $scope.user.email = user.email;
                        if (user.profilePic) {
                            $scope.thumbnail = meekbiz.settings.s3host + user.profilePic + '.jpg';
                        } else {
                            $scope.thumbnail = '/assets/img/profile/anon_person.png';
                        }
                    }
                }, true);
            }

            angular.element(window).on('beforeunload', function () {
                if ($scope.isEditting) {
                    return 'You are currently editing a form.';
                }
            });
            
            $scope.readOnly =  !!$window.readOnly;
        };

        $scope.cancel = function () {
            var user = $scope.$parent.user;
            $scope.user.name = user.name;
            $scope.user.email = user.email;

            if (user.profilePic) {
                $scope.thumbnail = meekbiz.settings.s3host + user.profilePic + '.jpg';
            } else {
                $scope.thumbnail = '/assets/img/profile/anon_person.png';
            }
            $scope.isLoading = false;
            $scope.photoHasChanged = false;
        };

        $scope.submit = function() {
            var deferred = $q.defer();
            deferred.promise.then(function (fileId) {
                var changeObject = {};
                if (fileId) {
                    changeObject.profilePic = fileId;
                }
                if ($scope.form.$dirty) {
                    $scope.isLoading = true;
                    if ($scope.form.registerName.$dirty) {
                        changeObject.name = $scope.user.name;
                    }
                    if ($scope.form.registerEmail.$dirty) {
                        changeObject.email = $scope.user.email;
                    }
                }
                if (!!changeObject) {
                    userService.updatePrivateProfile(changeObject).then(function () {
                        $scope.isLoading = false;
                        $scope.isEditting = false;
                    }, function () {
                        $scope.isLoading = false;
                    });
                } else {
                    $scope.isLoading = false;
                }
            });

            if ($scope.photoHasChanged) {
                $scope.isLoading = true;
                if ($scope.$parent.user && $scope.$parent.user.profilePic) {
                    s3fileService.updateFile($scope.$parent.user.profilePic, $scope.thumbnail).then(function () {
                        deferred.resolve();
                    }, function () {
                        deferred.resolve();
                    });
                } else {
                    s3fileService.createFile($scope.thumbnail).then(function (fileId) {
                        deferred.resolve(fileId);
                    }, function () {
                        deferred.resolve();
                    })
                }
            } else {
                deferred.resolve();
            }
        };

        $scope.init();

    }]);
    return mod;
});