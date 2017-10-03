define(['angular', 'common'], function (angular) {
    'use strict';
    var mod = angular.module('management.controllers.management', ['meekbiz.common']);
    mod.controller('ManagementCtrl', ['$scope', 'managementService',
        function ($scope, managementService) {
            $scope.init = function () {
                $scope.ingestNowDone = false;
                $scope.ingestNowError = false;
                $scope.reconnectDone = false;
                $scope.reconnectError = false;
                $scope.reindexDone = false;
                $scope.reindexError = false;
            };

            $scope.reconnectToSearch = function () {
                $scope.reconnectLoading = true;
                $scope.reconnectError = false;
                managementService.reInitSearch().then(function () {
                    $scope.reconnectDone = true;
                    $scope.reconnectLoading = false;
                }, function () {
                    $scope.reconnectLoading = false;
                    $scope.reconnectError = true;
                });
            };

            $scope.ingestNewMizNow = function () {
                $scope.ingestNewLoading = true;
                $scope.ingestNowError = false;
                managementService.synchWithNewMizes().then(function () {
                    $scope.ingestNowDone = true;
                    $scope.ingestNewLoading = false;
                }, function () {
                    $scope.ingestNewLoading = false;
                    $scope.ingestNowError = true;
                });
            };

            $scope.deleteSearchAndReingestAll = function () {
                $scope.deleteAllLoading = true;
                $scope.reindexError = false;
                managementService.rebuildSearchIndex().then(function () {
                    $scope.reindexDone = true;
                    $scope.deleteAllLoading = false;
                }, function () {
                    $scope.deleteAllLoading = false;
                    $scope.reindexError = true;
                });
            };

            $scope.init();
        }]);
    return mod;
});