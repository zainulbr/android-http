<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class Sensor extends CI_Model 
{
    public $id;
	public $status;

	public function __construct()
	{
        parent::__construct();
		$this->load->database();
    }
    public function getLatest()
    {
        $query = $this->db->query("SELECT * FROM pir WHERE time IN (SELECT MAX(time) FROM pir GROUP BY id)");
        return $query->result();
    }

    public function insert($id, $status)
    {
        $this->id = $id;
        $this->status = $status;

        $this->db->insert('pir', $this);
    }

    
}