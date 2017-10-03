define(['angular', '../services/userService', 'ui-bootstrap', 'ui-bootstrap-tpls'], function (angular) {
    'use strict';
    var mod = angular.module('common.directives.loginButton', ['common.services.user', 'ui.bootstrap', 'ui.bootstrap.tpls']);
    var loginModalCtrl = function ($scope, $modalInstance, userService) {
        $scope.init = function () {
            $scope.isLoading = false;
            $scope.error = false;
        };

        $scope.ok = function (login) {
            $scope.isLoading = true;
            $scope.error = false;
            userService.loginUser(login).then(function () {
                $scope.isLoading = false;
                $modalInstance.close();
            }, function () {
                $scope.isLoading = false;
                $scope.error = true;
            });

        };

        $scope.cancel = function () {
            $scope.isLoading = false;
            $modalInstance.dismiss('cancel');
        };

        $scope.init();
    };
    loginModalCtrl.$inject = ['$scope', '$modalInstance', 'userService'];
    mod.controller('loginModalCtrl',loginModalCtrl);
    mod.directive('loginButton', ['$modal',
        function ($modal) {
            return {
                restrict: 'E',
                transclude: true,
                scope: {},
                template: '<span ng-transclude></span>',
                link: function(scope, element) {
                    element.on('click', function () {
                        $modal.open({
                            templateUrl: '/assets/partials/common/loginModal.html',
                            controller: loginModalCtrl
                        });
                    });
                }
            };
        }]);
    return mod;
});