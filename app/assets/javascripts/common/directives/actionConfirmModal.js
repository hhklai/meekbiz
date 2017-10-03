define(['angular', 'ui-bootstrap', 'ui-bootstrap-tpls'], function (angular) {
    'use strict';
    var mod = angular.module('common.directives.actionConfirmModal', ['ui.bootstrap', 'ui.bootstrap.tpls']);
    var confirmModalCtrl = function ($scope, $modalInstance) {
        $scope.init = function () {
            $scope.isLoading = false;

            if (!$scope.confirmButtonText) {
                $scope.confirmButtonText = 'OK';
            }
            if (!$scope.cancelButtonText) {
                $scope.cancelButtonText = 'Cancel';
            }
            if (!$scope.mtitle) {
                $scope.mtitle = 'Are you sure?'
            }
            if (!$scope.mbody) {
                $scope.mbody = 'Press OK to confirm.'
            }

        };

        $scope.ok = function () {
            $scope.isLoading = true;
            $scope.error = false;
            if (!!$scope.onConfirm) {
                $scope.onConfirm().then(function () {
                    $scope.isLoading = false;
                    $modalInstance.close();
                }, function () {
                    $scope.isLoading = false;
                });
            } else {
                $scope.isLoading = false;
                $modalInstance.close();
            }

        };

        $scope.cancel = function () {
            $scope.isLoading = false;
            if (!!$scope.onCancel) {
                $scope.onCancel();
            }
            $modalInstance.dismiss('cancel');
        };

        $scope.init();
    };
    confirmModalCtrl.$inject = ['$scope', '$modalInstance'];
    mod.controller('confirmModalCtrl',confirmModalCtrl);
    mod.directive('actionConfirmModal', ['$modal',
        function ($modal) {
            return {
                restrict: 'E',
                transclude: true,
                scope: {
                    mtitle: '@',
                    mbody: '@',
                    confirmButtonText: '@',
                    cancelButtonText: '@',
                    onConfirm: '&',
                    onCancel: '&'
                },
                template: '<span ng-transclude></span>',
                link: function($scope, element) {

                    element.on('click', function () {
                        $modal.open({
                            scope: $scope,
                            template: '<div class="modal-header"><h3>{{mtitle}}</h3></div>' +
                                      '<div class="modal-body">{{mbody}}</div>' +
                                      '<div class="modal-footer">' +
                                          '<button class="has-spinner btn btn-primary" ng-class="{\'active\': isLoading}"' +
                                            ' data-ng-click="ok()">' +
                                            '{{confirmButtonText}}<span class="spinner"><i class="icon-spin icon-refresh"></i></span>' +
                                            '</button>' +
                                          '<button class="btn btn-warning" ng-click="cancel()">{{cancelButtonText}}</button>' +
                                      '</div>',
                            //size: 'sm',
                            controller: confirmModalCtrl
                        });
                    });
                }
            };
        }]);
    return mod;
});