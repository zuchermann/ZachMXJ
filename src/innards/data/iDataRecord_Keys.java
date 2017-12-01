package innards.data;

import innards.*;

public interface iDataRecord_Keys extends iKeyInterfaces
{
	/**
	 * This interface is for data keys used by <code>ClassifierPercepts</code>
	 * to store data in <code> DataRecords</code>
	 */
	public interface iClassifierPercept_Keys extends iKeyInterfaces
	{
		public interface DataFields extends iKeyInterfaces.DataFields
		{
			public static final Key RAW_DATA_KEY = new Key("raw data");
		}
	}
	
	public interface iUtteranceDataRecord_Keys extends iKeyInterfaces
	{
		public interface DataNames extends iKeyInterfaces.DataNames
		{
			public static final Key UTTERANCE_DATA_RECORD = new Key("utterance data record");
		}

		public interface DataFields extends iKeyInterfaces.DataFields 
		{
			/**
			 * contains float[] tmpCepstralArray = new float[real_frame_valid*num_filters];
			 */
			public static final Key UTTERANCE_CEPSTRALS = new Key("UTTERANCE_CEPSTRALS");
			/**
			 * contains real_frame_vaild @see UTTERANCE_CEPSTRALS
			 */
			public static final Key UTTERANCE_MAX_FRAME_VALID = new Key("UTTERANCE_MAX_FRAME_VALID");
		
		}
	}
	
	public interface iMouseDataRecord_Keys extends iKeyInterfaces
	{
		public interface DataNames extends iKeyInterfaces.DataNames
		{
			public static final Key MOUSE_DATA_RECORD = new Key("mouse data record");
		}
		
		public interface DataFields extends iKeyInterfaces.DataFields
		{
			//should map to a Vec2
			public static final Key MOUSE_XY = new Key("mouse XY");
			//should map to a float
			public static final Key MOUSE_SCROLL_WHEEL_MOVEMENT = new Key("scroll wheel movement");
			//should map to an int
			public static final Key MOUSE_BUTTON_ID = new Key("mouse button ID");
			//should map to a boolean
			public static final Key IS_MOUSE_DOWN = new Key("mouse is down");
			//should map to a boolean 
			public static final Key MOUSE_HAS_GROUND_PLANE_MAPPING = new Key("mouse has ground plane mapping");
			//should map to a vec3
			public static final Key MOUSE_GROUND_COORD = new Key("mouse ground coord");
			//should map to a boolean
			public static final Key MOUSE_HAS_RAY = new Key("mouse has ray");
			//should map to a Vec3
			public static final Key MOUSE_ORIGIN = new Key("mouse origin");
			//should map to a vec3
			public static final Key MOUSE_DIRECTION = new Key("mouse direction");
		}
	}
	
	public interface iPoseDataRecord_Keys extends iKeyInterfaces
	{
		public interface DataNames extends iKeyInterfaces.DataNames
		{
			public static final Key POSE_DATA_RECORD = new Key("pose data record");
		}
	}
	
	public interface iMotorMemoryDataRecord_Keys extends iKeyInterfaces
	{
		public interface DataNames extends iKeyInterfaces.DataNames
		{
			public static final Key MOTORMEMORY_DATA_RECORD = new Key("motor memory data record");
		}
		
		public interface DataFields extends iKeyInterfaces.DataFields
		{
			public static final Key MOTORMEMORY = new Key("motor memory");
		}
	}
	public interface iVisionDataRecord_Keys extends iKeyInterfaces
	{
		public interface DataNames extends iKeyInterfaces.DataNames
		{
			public static final Key VISION_DATA_RECORD = new Key("vision data record");
		}
		
		public interface DataFields extends iKeyInterfaces.DataFields
		{
			public static final Key VISUAL_LOCATION_KEY = new Key("visual location");
			
			public static final Key COLOR_KEY = new Key("color");
			
			public static final Key VISIBLE_SHAPE_KEY = new Key("shape of visual object");
		}
	}

	public interface iInputSensorDataRecord_Keys extends iKeyInterfaces
	{
		public interface DataNames extends iKeyInterfaces.DataNames
		{
			public static final Key MULTI_INPUT_DATA_RECORD = new Key("multiple input data record");
		}
	}
	
	public interface iProprioceptiveDataRecord_Keys extends iKeyInterfaces
	{
		public interface DataNames extends iKeyInterfaces.DataNames
		{
			public static final Key PROPRIOCEPTIVE_DATA_RECORD = new Key("proprioceptive data record");
		}
	}
}

