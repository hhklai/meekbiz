define(['angular', '../services/mizService', '../services/s3fileService'], function (angular) {
    'use strict';
    var mod = angular.module('common.controllers.mizBanner', ['common.services.miz', 'common.services.s3file']);
    mod.controller('MizBannerCtrl', ['$scope', 's3fileService', 'mizService', '$window', '$anchorScroll', '$location',
        function ($scope, s3fileService, mizService, $window, $anchorScroll, $location) {
            $scope.init = function () {
                if (!$window.readOnly) {
                    $scope.isEditting = false;
                    $scope.bannerPic = '';
                    $scope.bannerHasChanged = false;

                    $scope.$watch(function() {
                        return $scope.$parent.miz;
                    }, function(miz) {
                        if (miz && !$scope.bannerHasChanged) {
                            if (miz.bannerPic) {
                                $scope.bannerPic = meekbiz.settings.s3host + miz.bannerPic + '.jpg';
                            } else {
                                $scope.bannerPic = '/assets/img/miz/ninbg.jpg';
                            }
                        }
                    }, true);
                    $scope.isLoading = false;
                }

                angular.element(window).on('beforeunload', function () {
                    if ($scope.isEditting) {
                        $location.hash('mizBanner');
                        $anchorScroll();
                        return 'You are currently editing a form.';
                    }
                });
                $scope.readOnly =  !!$window.readOnly;
            };

            $scope.bannerEditCancel = function () {
                var miz = $scope.$parent.miz;
                if (miz.bannerPic) {
                    $scope.bannerPic = meekbiz.settings.s3host + miz.bannerPic + '.jpg';
                } else {
                    $scope.bannerPic = '/assets/img/miz/ninbg.jpg';
                }
                $scope.bannerHasChanged = false;
                $scope.isLoading = false;
            };

            $scope.submitBanner = function () {
                if ($scope.bannerHasChanged) {
                    $scope.isLoading = true;
                    if ($scope.$parent.miz && $scope.$parent.miz.bannerPic) {
                        s3fileService.updateFile($scope.$parent.miz.bannerPic, $scope.bannerPic).then(function () {
                            $scope.isLoading = false;
                            $scope.isEditting = false;
                        }, function () {
                            $scope.isLoading = false;
                        });
                    } else {
                        s3fileService.createFile($scope.bannerPic).then(function (fileId) {
                            mizService.updateMiz(window.mizTitle, {'bannerPic': fileId}).then(function (patchedMiz) {
                                $scope.$parent.miz.bannerPic = patchedMiz.bannerPic;
                                $scope.bannerHasChanged = false;
                                $scope.isLoading = false;
                                $scope.isEditting = false;
                            }, function () {
                                $scope.isLoading = false;
                            });
                        }, function () {
                            $scope.isLoading = false;
                        })
                    }
                } else {
                    $scope.isLoading = false;
                }
            };

            $scope.init();
    }]);
    return mod;
});