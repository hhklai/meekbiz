define(['angular', '../services/userService', 'ui-bootstrap', 'ui-bootstrap-tpls'], function (angular) {
    'use strict';
    var mod = angular.module('common.directives.registerButton', ['common.services.user', 'ui.bootstrap', 'ui.bootstrap.tpls']);
    var registerModalCtrl = function ($scope, $modalInstance, userService, $location) {
        $scope.init = function () {
            $scope.isLoading = false;
            $scope.location = $location;
            $scope.error = false;
        };

        $scope.ok = function (user) {
            $scope.isLoading = true;
            $scope.error = false;
            userService.registerUser(user).then(function () {
                $scope.isLoading = false;
                $modalInstance.close();
            }, function () {
                $scope.isLoading = false;
                $scope.error = true;
            });

        };

        $scope.cancel = function () {
            $scope.isLoading = true;
            $modalInstance.dismiss('cancel');
        }

        $scope.init();
    };
    registerModalCtrl.$inject = ['$scope', '$modalInstance', 'userService', '$location'];
    mod.controller('registerModalCtrl', registerModalCtrl);
    mod.directive('registerButton', ['$modal',
        function ($modal) {
            return {
                restrict: 'E',
                transclude: true,
                scope: {},
                template: '<span ng-transclude></span>',
                link: function(scope, element) {
                    element.on('click', function () {
                        $modal.open({
                            templateUrl: '/assets/partials/common/registerModal.html',
                            controller: registerModalCtrl
                        });
                    });
                }
            };
        }]);
    return mod;
});