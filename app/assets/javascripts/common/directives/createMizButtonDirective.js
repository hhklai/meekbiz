define(['angular', '../services/userService', '../services/mizService', '../controllers/mizSummaryController', '../controllers/mizBannerController',  '../controllers/mizDescriptionController', '../lib/playRoutes', 'ui-bootstrap', 'ui-bootstrap-tpls'], function (angular) {
    'use strict';
    var mod = angular.module('common.directives.createMizButton', ['common.services.user', 'common.services.miz', 'common.controllers.mizSummary', 'common.controllers.mizBanner', 'common.controllers.mizDescription', 'common.lib.playRoutes', 'ui.bootstrap', 'ui.bootstrap.tpls']);
    var createMizModalCtrl = function ($scope, $modalInstance, userService, mizService, s3fileService, $window, $q, playRoutes) {
        $scope.init = function () {
            $scope.page = 1;
            $scope.isLoading = false;
            $scope.error = false;
            $scope.miz = {};
            $scope.mizCreated = false;
            window.mizTitle = '';
        };

        $scope.createMiz = function (miz) {
            if (!userService.isLoggedIn()) {
                $modalInstance.dismiss('cancel');
            }

            if ($scope.mizCreated) {
                if (!$scope.isLoading) {
                    mizService.updateMiz(window.mizTitle, miz).then(function (patchedMiz) {
                        $scope.isLoading = false;
                        $scope.miz.title = patchedMiz.title;
                        window.mizTitle = patchedMiz.title;
                        $scope.miz.summary = patchedMiz.summary;
                        $scope.page = $scope.page + 1;
                    }, function () {
                        $scope.isLoading = false;
                        $scope.error = true;
                    });
                }
                return;
            }

            $scope.error = false;
            if (!$scope.isLoading) {
                $scope.isLoading = true;
                mizService.createMiz(miz).then(function (title) {
                    $scope.isLoading = false;
                    $scope.page = $scope.page + 1;
                    $scope.mizCreated = true;
                    window.mizTitle = title;
                }, function () {
                    $scope.isLoading = false;
                    $scope.error = true;
                });
            }
        };

        $scope.done = function () {
            $window.location.href = playRoutes.view.controllers.MizController.miz(userService.getUserName(), $scope.miz.title, true).relativeUrl + '#edit';
        };

        $scope.back = function () {
            $scope.page = $scope.page - 1;
        };

        $scope.next = function () {
            $scope.page = $scope.page + 1;
        };

        $scope.cancel = function () {
            $scope.isLoading = false;
            $modalInstance.dismiss('cancel');
        };

        $scope.discard = function () {
            if (!userService.isLoggedIn()) {
                $modalInstance.dismiss('cancel');
            }

            mizService.deleteMiz(userService.getUserName(), $scope.miz.title).then(function () {
                $modalInstance.dismiss('cancel');
            });
        };

        $scope.init();
    };
    createMizModalCtrl.$inject = ['$scope', '$modalInstance', 'userService', 'mizService', 's3fileService', '$window', '$q', 'playRoutes'];
    mod.controller('createMizModalCtrl',createMizModalCtrl);
    mod.directive('createMizButton', ['$modal',
        function ($modal) {
            return {
                restrict: 'E',
                transclude: true,
                scope: {},
                template: '<div ng-transclude></div>',
                link: function(scope, element) {
                    element.on('click', function () {
                        $modal.open({
                            templateUrl: '/assets/partials/common/createMizModal.html',
                            controller: createMizModalCtrl,
                            size: 'lg',
                            backdrop: 'static'
                        });
                    });
                }
            };
        }]);
    return mod;
});