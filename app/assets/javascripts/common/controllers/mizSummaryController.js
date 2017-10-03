define(['angular', '../services/mizService', '../services/s3fileService', '../lib/playRoutes'], function (angular) {
    'use strict';
    var mod = angular.module('common.controllers.mizSummary', ['common.services.miz', 'common.services.s3file', 'common.lib.playRoutes']);
    mod.controller('MizSummaryCtrl', ['$scope', 's3fileService', 'mizService', 'playRoutes', '$window', '$q', '$anchorScroll', '$location',
        function ($scope, s3fileService, mizService, playRoutes, $window, $q, $anchorScroll, $location) {

            $scope.init = function () {
                if (!$window.readOnly) {
                    $scope.isEditting = false;
                    $scope.thumbnail = '';
                    $scope.isLoading = false;
                    $scope.photoHasChanged = false;
                    $scope.miz = {};
                    $scope.$watch(function() {
                        return $scope.$parent.miz;
                    }, function(miz) {
                        if (miz && !$scope.form.$dirty && !$scope.photoHasChanged) {
                            $scope.miz.title = miz.title;
                            $scope.miz.summary = miz.summary;
                            if (miz.thumbnail) {
                                $scope.thumbnail = meekbiz.settings.s3host + miz.thumbnail + '.jpg';
                            } else {
                                $scope.thumbnail = '/assets/img/miz/round_question_mark.jpg';
                            }
                        }
                    }, true);
                }

                angular.element(window).on('beforeunload', function () {
                    if ($scope.isEditting) {
                        $location.hash('mizSummary');
                        $anchorScroll();
                        return 'You are currently editing a form.';
                    }
                });
                $scope.readOnly =  !!$window.readOnly;
            };

            $scope.cancel = function () {
                var miz = $scope.$parent.miz;
                $scope.miz.title = miz.title;
                $scope.miz.summary = miz.summary;

                if (miz.thumbnail) {
                    $scope.thumbnail = meekbiz.settings.s3host + miz.thumbnail + '.jpg';
                } else {
                    $scope.thumbnail = '/assets/img/miz/round_question_mark.jpg';
                }
                $scope.isLoading = false;
                $scope.photoHasChanged = false;
           };

            $scope.submit = function() {
                if ($scope.form.$invalid) {
                    return;
                }
                
                var deferred = $q.defer();
                deferred.promise.then(function (fileId) {
                    var changeObject = {};
                    if (fileId) {
                        changeObject.thumbnail = fileId;
                    }
                    if ($scope.form.$dirty) {
                        $scope.isLoading = true;
                        if ($scope.form.title.$dirty) {
                            changeObject.title = $scope.miz.title;
                        }
                        if ($scope.form.summary.$dirty) {
                            changeObject.summary = $scope.miz.summary;
                        }
                    }
                    if (!!changeObject) {
                        mizService.updateMiz(window.mizTitle, changeObject).then(function (patchedMiz) {
                            if (patchedMiz.title != window.mizTitle) {
                                $window.location.href = playRoutes.view.controllers.MizController.miz(window.username, patchedMiz.title, true).relativeUrl
                            }
                            $scope.isLoading = false;
                            $scope.isEditting = false;
                            $scope.photoHasChanged = false;
                            $scope.$parent.miz.title = patchedMiz.title;
                            $scope.$parent.miz.summary = patchedMiz.summary;
                            $scope.$parent.miz.thumbnail = patchedMiz.thumbnail;
                        }, function () {
                            $scope.isLoading = false;
                        });
                    } else {
                        $scope.isLoading = false;
                    }
                });

                if ($scope.photoHasChanged) {
                    $scope.isLoading = true;
                    if ($scope.$parent.miz && $scope.$parent.miz.thumbnail) {
                        s3fileService.updateFile($scope.$parent.miz.thumbnail, $scope.thumbnail).then(function () {
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