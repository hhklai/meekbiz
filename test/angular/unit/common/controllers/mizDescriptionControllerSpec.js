define(['angular-mock', 'common'], function () {
    describe("mizDescriptionController", function () {
        var mizService, mockWin, scope, controller, q, deferred, promiseResponse, promiseError;
        var username = 'hello', title = 'world';
        beforeEach(function () {
            module('common.controllers.mizDescription');
        });
        beforeEach(function () {
            inject(function ($rootScope, $controller, $q) {
                scope = $rootScope.$new();
                q = $q;
                mizService = {
                    getPrivateMiz : function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    },
                    updateMiz: function () {
                        deferred = q.defer();
                        if (promiseError) {
                            deferred.reject(promiseResponse);
                        } else {
                            deferred.resolve(promiseResponse);
                        }
                        return deferred.promise;
                    }
                };
                spyOn(mizService, 'getPrivateMiz').andCallThrough();
                spyOn(mizService, 'updateMiz').andCallThrough();

                mockWin = {
                    readOnly: false
                };

                controller = $controller;

            });
            window.meekbiz = {};
            window.meekbiz.settings = {};
            window.meekbiz.settings.s3host = "http://s3.amazon.com/";
            window.username =  username;
            window.mizTitle = title;
        });

        describe('when form send button is pressed and miz description has changed', function () {
            beforeEach(function () {
                promiseError = false;
                scope.$parent = {
                    miz: {
                        contentBody: ''
                    }
                };
                scope.form = {
                    '$dirty': false,
                    'description': {
                        '$dirty': false
                    }
                };
                controller('MizDescriptionCtrl', {'$scope': scope, 'mizService': mizService, '$window': mockWin});
                scope.$digest();
                promiseResponse = {};
                scope.form = {
                    '$dirty': true,
                    'description': {
                        '$dirty': true
                    }
                };

                scope.miz = {
                    contentBody: '<p>hello</p>'
                }

                scope.submit();
                scope.$digest();
            });
            it('should call mizService', function () {
                expect(mizService.updateMiz).toHaveBeenCalled();
                expect(mizService.updateMiz.mostRecentCall.args[0]).toBe(title);
                expect(mizService.updateMiz.mostRecentCall.args[1].contentBody).toBe('<p>hello</p>');
            })

        });
    });
});