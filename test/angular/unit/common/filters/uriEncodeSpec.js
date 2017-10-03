define(['angular-mock', 'common'], function () {
    describe("uriEncode", function () {
        var filter;
        beforeEach(function () {
            module('common.filters.uriEncode');
        });
        beforeEach(function () {
            inject(function ($filter) {
                filter = $filter;
            });
        });
        it('should uri encode a string', function () {
            var string = 'crazy characters /@{}=[]+%!:';
            var result = filter('uriEncode')(string);
            expect(result).toBe('crazy%20characters%20%2F%40%7B%7D%3D%5B%5D%2B%25!%3A');
        });
    });
});