<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Firebase extends CI_Model 
{
	public $token;

	public function __construct()
	{
        parent::__construct();
		$this->load->database();
    }
    
    public function getToken($id)
    {
        $this->db->select('token');
        $this->db->from('firebase');
        $this->db->where('id',$id);
        // return $this->db->get()->row()->token;
        return $this->db->get()->result();
        
    }

    public function insert($id, $token)
    {
        $this->token = $token;

        $this->db->where('id',$id);
        return $this->db->update('firebase',$this);       
    }
}