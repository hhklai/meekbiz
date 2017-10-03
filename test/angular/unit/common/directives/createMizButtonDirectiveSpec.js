define(['angular-mock','common'], function () {
    describe("createMizButton", function () {
        var element, $scope, modalMock;
        beforeEach(function () {
            module('common.directives.createMizButton');
        });
        beforeEach(function () {
            inject(function ($compile, $rootScope, $modal) {
                $scope = $rootScope;
                element =  angular.element('<create-miz-button>Create a new service</create-miz-button>');
                $compile(element)($rootScope);

                modalMock = $modal;
                spyOn(modalMock, 'open');
            })
        });

        describe('when clicking on create miz button', function () {
            it("should open a modal", function () {
                element.triggerHandler('click');
                expect(modalMock.open).toHaveBeenCalled();
                expect(modalMock.open.mostRecentCall.args[0].templateUrl).toBe('/assets/partials/common/createMizModal.html');
            });
        });

        describe('when click createMiz in mizModal', function () {
            var controller, modalInstance, mizService, userService, mockWindow, mockPlayRoutes, deferred;
            beforeEach(function () {
                modalInstance = {
                    'open': jasmine.createSpy(),
                    'dismiss': jasmine.createSpy()
                };
                mizService = {
                    'createMiz': function () {
                        return deferred.promise
                    }
                };
                spyOn(mizService, 'createMiz').andCallThrough();

            });
            describe('and when not logged in', function () {
                beforeEach(function () {
                    inject(function ($controller, $q) {
                        deferred = $q.defer();

                        userService = {
                            'isLoggedIn': function () {
                                return false;
                            },
                            'getUserName': function () {
                                return 'aName';
                            }
                        };
                        controller = $controller('createMizModalCtrl', {'$scope':$scope,'$modalInstance':modalInstance,'userService':userService, 'mizService':mizService, '$window':mockWindow, 'playRoutes':mockPlayRoutes });
                        $scope.createMiz({'title':'title','description':'adescription'});
                    });
                });
                it("should close the modal", function () {
                    expect(modalInstance.dismiss).toHaveBeenCalled();
                });
            });
            describe('and when is logged in', function () {
                beforeEach(function () {
                    inject(function ($controller, $q) {
                        deferred = $q.defer();

                        userService = {
                            'isLoggedIn': function () {
                                return true;
                            },
                            'getUserName': function () {
                                return 'aName';
                            }
                        };
                        controller = $controller('createMizModalCtrl', {'$scope':$scope,'$modalInstance':modalInstance,'userService':userService, 'mizService':mizService, '$window':mockWindow, 'playRoutes':mockPlayRoutes });

                        $scope.createMiz({'title':'title','description':'adescription'});
                        deferred.resolve('title');
                    });
                });
                it("should call mizService create miz", function () {
                    expect(mizService.createMiz).toHaveBeenCalled();
                    expect(mizService.createMiz.mostRecentCall.args[0].title).toBe('title');
                    expect(mizService.createMiz.mostRecentCall.args[0].description).toBe('adescription');
                });
            });

        });

        describe('when click discard in mizModal', function () {
            var controller, modalInstance, mizService, userService, mockWindow, mockPlayRoutes, deferred;
            beforeEach(function () {
                modalInstance = {
                    'open': jasmine.createSpy(),
                    'dismiss': jasmine.createSpy()
                };
                mizService = {
                    'deleteMiz': function (username, title) {
                        return deferred.promise;
                    }
                };
                spyOn(mizService, 'deleteMiz').andCallThrough();

            });
            describe('and when not logged in', function () {
                beforeEach(function () {
                    inject(function ($controller, $q) {
                        deferred = $q.defer();

                        userService = {
                            'isLoggedIn': function () {
                                return false;
                            },
                            'getUserName': function () {
                                return 'aName';
                            }
                        };
                        controller = $controller('createMizModalCtrl', {'$scope':$scope,'$modalInstance':modalInstance,'userService':userService, 'mizService':mizService, '$window':mockWindow, 'playRoutes':mockPlayRoutes });
                        $scope.miz = {
                            title: 'aTitle'
                        };
                        $scope.discard();
                    });
                });
                it("should close the modal", function () {
                    expect(modalInstance.dismiss).toHaveBeenCalled();
                });
            });
            describe('and when is logged in', function () {
                beforeEach(function () {
                    inject(function ($controller, $q) {
                        deferred = $q.defer();

                        userService = {
                            'isLoggedIn': function () {
                                return true;
                            },
                            'getUserName': function () {
                                return 'aName';
                            }
                        };
                        controller = $controller('createMizModalCtrl', {'$scope':$scope,'$modalInstance':modalInstance,'userService':userService, 'mizService':mizService, '$window':mockWindow, 'playRoutes':mockPlayRoutes });
                        $scope.miz = {
                            title: 'aTitle'
                        };
                        $scope.discard();
                        deferred.resolve();
                        $scope.$digest();
                    });
                });
                it("should call mizService discard miz", function () {
                    expect(mizService.deleteMiz).toHaveBeenCalled();
                    expect(mizService.deleteMiz.mostRecentCall.args[0]).toBe('aName');
                    expect(mizService.deleteMiz.mostRecentCall.args[1]).toBe('aTitle');
                });
                it("should close the modal", function () {
                    expect(modalInstance.dismiss).toHaveBeenCalled();
                });
            });

        });
    });
});