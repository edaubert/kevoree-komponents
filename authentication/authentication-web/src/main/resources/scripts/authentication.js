/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 04/12/13
 * Time: 14:32
 *
 * @author Erwan Daubert
 * @version 1.0
 */
jQuery(document).ready(function () {
    jQuery("#authentication-form").submit(function (e) {
        e.preventDefault();
        console.log("trying to authenticate...");
        var formData = jQuery(this).serialize();
        formData.password = make_base_auth(formData.login, formData.password);

        jQuery('#message').empty();

        //Perform an asynchronous HTTP (Ajax) request.
        jQuery.ajax({
            //This URL to which the request is sent
            url: "authentication",
            //The data which will be passed along with the request.
            data: formData,
            //The type of request to make ( GET or POST)
            type: "POST",
            //The type of data which is returned.
            dataType: "json"
            //The callback function which is called if the request has been successful.
        }).done(function (data) {
                //You would generally make changes to the DOM at this stage, maybe
                //adding a success message to the form.
                alert(data);
                //The callback function which is called is the request fails.
            }).fail(function () {
                //You would generally make changes to the DOM at this stage, maybe
                //adding a failure message to the form.
                jQuery('#message').html('An error occurred, please try again.');
            });
    });
});

function make_base_auth(user, password) {
    var tok = user + ':' + password;
    var hash = btoa(tok);
    return "Basic " + hash;
}

/*
 $('#userLogin').submit(function(e) {
 e.preventDefault();

 var username = $('#inputEmail').val();

 $.ajax({
 url      : '/cgi-bin/salt.cgi',
 type     : 'GET',
 async    : false,
 data     : { email: username },
 dataType : 'json'
 }).done(function(json) {

 var salt = json["salt"];
 var hash = hex_sha256($('#inputPassword').val());
 var password = hex_hmac_sha256(salt, hash);

 $.ajax({
 url      : '/cgi-bin/authenticate.cgi',
 type     : 'GET',
 headers  : { Authorization: make_base_auth(username, password) },
 success  : function(){
 setTimeout(function() { window.location = '/'; }, 2000);
 },
 error    : function(){ console.log('error'); }
 });

 });

 });

 $(".logout").on('click', function(e) {
 e.preventDefault();

 $.ajax({
 url      : '/cgi-bin/authenticate.cgi',
 type     : 'POST',
 username : 'anony',
 password : 'mouse'
 });
 });*/
