define(['angular-mock', 'common'], function () {
    describe("mizTitleNotTakenDirective", function () {
        var element, $scope, mizService, form, q, deferred, promiseResponse;
        beforeEach(function () {
            module('common.directives.forms.validators.mizTitleNotTaken', function ($provide) {
                $provide.decorator('mizService', function ($delegate) {
                    $delegate.checkIfMizExists = function () {
                        deferred = q.defer();
                        deferred.resolve(promiseResponse);
                        return deferred.promise;
                    }
                    spyOn($delegate, 'checkIfMizExists').andCallThrough();
                    return $delegate;
                });
            });

            inject(function (_mizService_) {
                mizService = _mizService_;
            });
        });

        describe('when title not taken directive has no ignore string', function () {
            beforeEach(function () {
                inject(function ($compile, $rootScope, $q) {
                    $scope = $rootScope;
                    q = $q;
                    element = angular.element('<form name="form">' +
                        '<input type="text" id="title" name="title" ng-model="miz.title" miz-title-not-taken />' +
                        '</form>');
                    $scope.miz = { title: null };
                    $compile(element)($scope);
                    $scope.$digest();
                    form = $scope.form;
                })
            });

            describe('when inputting a title, and found in the database', function () {
                beforeEach(function () {
                    promiseResponse = {'data': {'found': true}}
                });
                it("should call backend to check and mark invalid", function () {
                    form.title.$setViewValue('hello');
                    $scope.$digest();
                    expect(mizService.checkIfMizExists).toHaveBeenCalled();
                    expect(form.title.$valid).toBe(false);
                });
            });

            describe('when inputting a title, and not found in the database', function () {
                beforeEach(function () {
                    promiseResponse = {'data': {'found': false}};
                });
                it("should call backend and mark valid", function () {
                    form.title.$setViewValue('hello');
                    $scope.$digest();
                    expect(mizService.checkIfMizExists).toHaveBeenCalled();
                    expect(form.title.$valid).toBe(true);
                });
            });
        });

        describe('when name not taken directive has ignore string', function () {
            beforeEach(function () {
                inject(function ($compile, $rootScope, $q) {
                    $scope = $rootScope;
                    q = $q;
                    element = angular.element('<form name="form">' +
                        '<input type="text" id="title" name="title" ng-model="miz.title" miz-title-not-taken="originalMiz.title" />' +
                        '</form>');
                    $scope.miz = { title: null };
                    $scope.originalMiz = { title: "aTitle" };
                    $compile(element)($scope);
                    $scope.$digest();
                    form = $scope.form;
                })
            });
            it('should call backend if input title doesn\'t match original title', function () {
                promiseResponse = {'data': {'found': false}};
                form.title.$setViewValue('hello');
                $scope.$digest();
                expect(mizService.checkIfMizExists).toHaveBeenCalled();
                expect(form.title.$valid).toBe(true);
            });
            it('should return valid if input title matches original title', function () {
                form.title.$setViewValue('aTitle');
                expect(mizService.checkIfMizExists).not.toHaveBeenCalled();
                expect(form.title.$valid).toBe(true);
            });
        });

    });
});