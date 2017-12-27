<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Welcome extends CI_Controller {

	/**
	 * Index Page for this controller.
	 *
	 * Maps to the following URL
	 * 		http://example.com/index.php/welcome
	 *	- or -
	 * 		http://example.com/index.php/welcome/index
	 *	- or -
	 * Since this controller is set as the default controller in
	 * config/routes.php, it's displayed at http://example.com/
	 *
	 * So any other public methods not prefixed with an underscore will
	 * map to /index.php/welcome/<method_name>
	 * @see https://codeigniter.com/user_guide/general/urls.html
	 */
	public function index()
	{
		$this->load->view('welcome_message');
	}
	
	 public function __construct()
	{
        parent::__construct();
		$this->load->model('sensor');
		$this->load->model('firebase');		
    }

    public function latest(){
        $latest = $this->sensor->getLatest();
        echo json_encode($latest);
    }

    public function insert($id = NULL, $status = 0)
    {
        echo '{"id":'.$id.',"status":'.$status.'}';

		$this->sensor->insert($id, $status);
		
		if ($status == 1) {
			$message = array("message" => "Warning your home in inscure");
			$dbquery = $this->firebase->getToken(1);
			foreach ($dbquery as $row) {
				$token = $row->token;
			}
	
			// echo '{"tokens":"'$token'}';
			$message_status = $this->send_notification($token, $message);
			echo '{"tokens":,"message":'.$message_status.'}';
			echo $token[0];
		}
	}
	
	public function registerToken($id, $token)
    {
		if ($id == null || $id == "" ) {
			$id = 1;
		}
		if ($token == null || $token == "" ) {
			$message = array("error" => "token is null");		
			echo json_encode($message);
			return;
		}

		$result = $this->firebase->insert($id, $token);
		if ($result) {	
			$message = array("result" => "Succes update");		
		} else {
			$message = array("error" => "error update");		
		}
		
		echo json_encode($message);
		
	}
	
	
	function send_notification ($tokens, $message)
	{
		$url = 'https://fcm.googleapis.com/fcm/send';
		$fields = array(
			 'registration_ids' => $tokens,
			 'data' => $message
			);

		$headers = array(
			'Authorization:key = AAAAirbQwq0:APA91bHvKAJdr3-lWAtOj0BRVQCUHno0DhNasDfp3OkxOkmXAzeHoiwhLjeQZZnB5enx2dWGQ1rKEWsobjqoqmvY9h0yU9MlJPjb6WpKhmLDd1aMYx5OOkJTiWGHey_GVTztVsYZG2yB ',
			'Content-Type: application/json'
			);

	   $ch = curl_init();
       curl_setopt($ch, CURLOPT_URL, $url);
       curl_setopt($ch, CURLOPT_POST, true);
       curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
       curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
       curl_setopt ($ch, CURLOPT_SSL_VERIFYHOST, 0);  
       curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
       curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
       $result = curl_exec($ch);           
       if ($result === FALSE) {
           die('Curl failed: ' . curl_error($ch));
       }
       curl_close($ch);
       return $result;
	}
}