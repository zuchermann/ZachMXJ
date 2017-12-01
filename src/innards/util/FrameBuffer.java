package innards.util;

import java.io.*;
import java.nio.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

// minus the swing

public class FrameBuffer {
	public static boolean readRawAlpha = true;
	public int[] data;

	static public boolean getHolds = false;

	public int w, h;

	public FrameBuffer(int w, int h)
	{
		this(w, h, true);
	}

	public FrameBuffer(int w, int h, String im)
	{
		this(w, h, true);
		this.readRaw(im);
	}

	public FrameBuffer(int w, int h, boolean createImage)
	{
		rebuild(w, h, createImage);
	}

	public FrameBuffer(int w, int h, ByteBuffer b)
	{
		this.w = w;
		this.h = h;
		IntBuffer i = b.asIntBuffer();
		if (i.hasArray())
		{
			System.out.println(" direct access ");
			data = i.array();
		}
		else
        {
            System.out.println(" had to copy data");
            data = new int[w*h];
            i.rewind();
            i.get(data);
        }
	}

    public void fromByteBuffer (int w, int h, ByteBuffer b)
    {
        IntBuffer i = b.asIntBuffer();
        if (i.hasArray())
        {
            System.out.println(" direct access ");

            if (data != i.array()) {
                System.out.println(" had to reset data");
                data = i.array();
            }
        }
        else
        {
            System.out.println(" had to copy data");
            if (w*h != this.w*this.h) {
                System.out.println(" had to reallocate");
                data = new int[w*h];
            }
            i.rewind();
            i.get(data);
        }
        this.w = w;
        this.h = h;

    }

	public void rebuild(int w, int h, boolean createImage)
	{
		this.w = w;
		this.h = h;
		data = new int[w * h];
	}

	public void rebuildImage()
	{
	}
	

	public void clear(int to) {
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++) {
				set(x, y, to);
			}
	}

	public void multiply(float f) {
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++) {
				int c = get(x, y);
				set(
					x,
					y,
					colour((int) (red(c) * f), (int) (green(c) * f), (int) (blue(c) * f)));
			}
	}
	public void multiply(int x, int y, float f) {
		int c = get(x, y);
		set(
			x,
			y,
			colour((int) (red(c) * f), (int) (green(c) * f), (int) (blue(c) * f)));
	}
	public int getWidth() {
		return w;
	}
	public int getHeight() {
		return h;
	}
	public void set(int x, int y, int z) {
		x = (x + w) % w;
		y = (y + h) % h;
		data[y * w + x] = z;
	}

	public void setFuz(int x, int y, int z) {
		set(x, y, z);
		int a;
		a = get(x + 1, y);
		set(
			x + 1,
			y,
			colour(
				(red(z) + red(a)) / 2,
				(green(z) + green(a)) / 2,
				(blue(z) + blue(a)) / 2));
		a = get(x - 1, y);
		set(
			x - 1,
			y,
			colour(
				(red(z) + red(a)) / 2,
				(green(z) + green(a)) / 2,
				(blue(z) + blue(a)) / 2));
		a = get(x, y + 1);
		set(
			x,
			y + 1,
			colour(
				(red(z) + red(a)) / 2,
				(green(z) + green(a)) / 2,
				(blue(z) + blue(a)) / 2));
		a = get(x, y - 1);
		set(
			x,
			y - 1,
			colour(
				(red(z) + red(a)) / 2,
				(green(z) + green(a)) / 2,
				(blue(z) + blue(a)) / 2));
		a = get(x + 1, y + 1);
		set(
			x + 1,
			y + 1,
			colour(
				(red(z) + red(a)) / 2,
				(green(z) + green(a)) / 2,
				(blue(z) + blue(a)) / 2));
		a = get(x + 1, y - 1);
		set(
			x + 1,
			y - 1,
			colour(
				(red(z) + red(a)) / 2,
				(green(z) + green(a)) / 2,
				(blue(z) + blue(a)) / 2));
		a = get(x - 1, y + 1);
		set(
			x - 1,
			y + 1,
			colour(
				(red(z) + red(a)) / 2,
				(green(z) + green(a)) / 2,
				(blue(z) + blue(a)) / 2));
		a = get(x - 1, y - 1);
		set(
			x - 1,
			y - 1,
			colour(
				(red(z) + red(a)) / 2,
				(green(z) + green(a)) / 2,
				(blue(z) + blue(a)) / 2));
	}

	public int get(int x, int y) {
		//	    System.out.println(" get <"+x+"> <"+y+"> <"+w+"> <"+h+">"+" "+(data[y*w+x]));
		if (getHolds) {
			if (x > (w - 1))
				x = w - 1;
			if (y > (w - 1))
				y = w - 1;
			if (x < 0)
				x = 0;
			if (y < 0)
				y = 0;
		}
		x = (x + w) % w;
		y = (y + h) % h;
		return data[y * w + x];
	}

	static public int blue(int z) {
		return z & 255;
	}

	static public int green(int z) {
		return (z >> 8) & 255;
	}

	static public int red(int z) {
		return (z >> 16) & 255;
	}

	static public int alpha(int z) {
		return (z >> 24) & 255;
	}
	static public float intensity(int c) {
		return ((c & 255) + ((c >> 8) & 255) + ((c >> 16) & 255)) / (255.0f * 3);
	}
	static public int colour(int r, int g, int b, int a) {
		return b | (g << 8) | (r << 16) | (a << 24);
	}
	static public int colour(byte r, byte g, byte b, byte a) {
		return (b & 0xff) | ((g & 0xff) << 8) | ((r & 0xff) << 16) | ((a & 0xff) << 24);
	}
	static public int colour(float f)
	{
		int i = (int)(f*255);
		return colour(i,i,i);
	}

	static public int safeColour(int r, int g, int b) {
		if (r < 0)
			r = 0;
		if (r > 255)
			r = 255;
		if (g < 0)
			g = 0;
		if (g > 255)
			g = 255;
		if (b < 0)
			b = 0;
		if (b > 255)
			b = 255;
		return colour(r, g, b);
	}
	static public int colour(int r, int g, int b) {
		return b | (g << 8) | (r << 16) | (255 << 24);
	}
	static public int colour(byte r, byte g, byte b) {
		return (b & 0xff) | ((g & 0xff) << 8) | ((r & 0xff) << 16) | (255 << 24);
	}

	
	public void colourReplace(int targetr, int targetg, int targetb, int rep) {
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++) {
				int c = get(x, y);
				if ((targetr == red(c)) && (targetg == green(c)) && (targetb == blue(c))) {
					set(x, y, rep);
				}
			}
	}

	public void copyFrom(FrameBuffer from) {
		if ((from.w != w) || (from.h != h))
			throw new IllegalArgumentException(
				" dimension mismatch in FrameBuffer.copyFrom. w<"
					+ w
					+ "> != <"
					+ from.w
					+ "> or <"
					+ h
					+ "> != <"
					+ from.h
					+ ">");
		System.arraycopy(from.data, 0, this.data, 0, this.data.length);
	}


	public void readRaw(String filename) {
		int x = -1;
		int y = -1;
		System.out.println(" readRaw <" + filename + ">");
		try {
			RandomAccessFile raf = new RandomAccessFile(new File(filename), "r");
			byte[] readArray = new byte[(int) raf.length()];
			raf.readFully(readArray);
			int read = 0;
			System.out.println(
				" reading <" + raf.length() + "(" + ((int) raf.length()) + ")");
			for (y = 0; y < h; y++) {
				for (x = 0; x < w; x++) {
					byte a = (byte) 255;
					if (readRawAlpha) {
						a = readArray[read];
						read++;
					}
					byte r = readArray[read];
					read++;
					byte g = readArray[read];
					read++;
					byte b = readArray[read];
					read++;

					int ri = (r < 0 ? (((int) r) + 256) : r);
					int gi = (g < 0 ? (((int) g) + 256) : g);
					int bi = (b < 0 ? (((int) b) + 256) : b);

					int z;
					if (!readRawAlpha)
						z = colour(ri, gi, bi);
					else
						z = colour(ri, gi, bi, (a < 0 ? (((int) a) + 256) : a));
					set(x, y, z);
				}
			}
			raf.close();
		} catch (Exception ex) {
			System.out.println(" at :" + x + " " + y);
			ex.printStackTrace();
		}
		System.out.println(" readRaw <" + filename + "> finished");
	}

	public int[] getIntArray() {
		return data;
	}


	public void lineFast(int x0, int y0, int x1, int y1, int pix) {
		int dy = y1 - y0;
		int dx = x1 - x0;
		int stepx, stepy;

		if (dy < 0) {
			dy = -dy;
			stepy = -this.w;
		} else {
			stepy = this.w;
		}
		if (dx < 0) {
			dx = -dx;
			stepx = -1;
		} else {
			stepx = 1;
		}
		dy <<= 1;
		dx <<= 1;

		y0 *= this.w;
		y1 *= this.w;
		this.data[x0 + y0] = pix;
		if (dx > dy) {
			int fraction = dy - (dx >> 1);
			while (x0 != x1) {
				if (fraction >= 0) {
					y0 += stepy;
					fraction -= dx;
				}
				x0 += stepx;
				fraction += dy;
				this.data[x0 + y0] = pix;
			}
		} else {
			int fraction = dx - (dy >> 1);
			while (y0 != y1) {
				if (fraction >= 0) {
					x0 += stepx;
					fraction -= dy;
				}
				y0 += stepy;
				fraction += dx;
				this.data[x0 + y0] = pix;
			}
		}
	}

	// this is the world's worst drawline algorithmn
	// but its too late to find bressy somewhere on the net.
	// argh.
	public void drawLine(int x, int y, int nx, int ny, int c) {
		int iter = (int) Math.abs(nx - x) + (int) Math.abs(ny - y);
		float incx = (nx - x) / (float) iter;
		float incy = (ny - y) / (float) iter;
		float ix = x;
		float iy = y;
		int lx = x - 1;
		int ly = y - 1;
		set(lx, ly, c);

		for (int i = 0; i < iter; i++) {
			lx = (int) ix;
			ly = (int) iy;

			ix += incx;
			iy += incy;

			if (((int) ix != lx) || ((int) iy != ly))
				set((int) ix, (int) iy, c);
		}
	}

	// this is the world's worst drawline algorithmn
	// but its too late to find bressy somewhere on the net.
	// argh.
	public void drawLineMul(int x, int y, int nx, int ny, float f) {
		int iter = (int) Math.abs(nx - x) + (int) Math.abs(ny - y);
		float incx = (nx - x) / (float) iter;
		float incy = (ny - y) / (float) iter;
		float ix = x;
		float iy = y;
		int lx = x - 1;
		int ly = y - 1;
		multiply(lx, ly, f);

		for (int i = 0; i < iter; i++) {
			lx = (int) ix;
			ly = (int) iy;

			ix += incx;
			iy += incy;

			if (((int) ix != lx) || ((int) iy != ly))
				multiply((int) ix, (int) iy, f);
		}
	}

	Writer PSOutput = null;

	public void setPSParameters(Writer os) {
		PSOutput = os;
	}

	public void emitLinePS(int x, int y, int nx, int ny) {
		if (PSOutput != null) {
			try {
				PSOutput.write(
					"gsave "
						+ x
						+ " "
						+ y
						+ " moveto "
						+ nx
						+ " "
						+ ny
						+ " lineto stroke grestore \n");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			System.out.println(" warning: emitLinePS called before SetPSParmeters called ");
		}
	}

	public void saveRaw(String filename) {
		System.out.println(" saving framebuffer to file <" + filename + ">");
		try
		{
			FileChannel rwChannel = new RandomAccessFile(new File(filename), "rw").getChannel();
			ByteBuffer wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, (int)data.length*4);
			wrBuf.asIntBuffer().put(IntBuffer.wrap(data));
			rwChannel.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println(" saving framebuffer to file <" + filename + "> complete");
	}

	static public int blendTowards(int from, double alpha, int to) {
		return colour(
			(int) (red(from) * (1 - alpha) + red(to) * alpha),
			(int) (green(from) * (1 - alpha) + green(to) * alpha),
			(int) (blue(from) * (1 - alpha) + blue(to) * alpha),
			(int) (alpha(from) * (1 - alpha) + alpha(to) * alpha));
	}
	static public int massiveBlendTowards(int from, double alpha, int to) {
		float f1 = intensity(from);
		float f2 = intensity(to);
		double a = f2 / (f1 + f2 + 1e-6);
		a = a * alpha + 1 * (1 - alpha);
		return blendTowards(from, a, to);
	}

	public void black() {
		int c = this.colour(0, 0, 0, 255);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				this.data[y * w + x] = c;
			}
		}
	}

	float[] cacheHSV = new float[3];
	int cacheHSV_z = -1;
	
	public float hue(int z)
	{
		if (z == cacheHSV_z) return cacheHSV[0];
		else RGBtoHSB(red(z), green(z), blue(z), cacheHSV);
		cacheHSV_z = z;
		return cacheHSV[0];
	}
	public float saturation(int z)
	{
		if (z == cacheHSV_z) return cacheHSV[1];
		else RGBtoHSB(red(z), green(z), blue(z), cacheHSV);
		cacheHSV_z = z;
		return cacheHSV[1];
	}
	
	public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
	   float hue, saturation, brightness;
	   if (hsbvals == null) {
		   hsbvals = new float[3];
	   }
		   int cmax = (r > g) ? r : g;
	   if (b > cmax) cmax = b;
	   int cmin = (r < g) ? r : g;
	   if (b < cmin) cmin = b;

	   brightness = ((float) cmax) / 255.0f;
	   if (cmax != 0)
		   saturation = ((float) (cmax - cmin)) / ((float) cmax);
	   else
		   saturation = 0;
	   if (saturation == 0)
		   hue = 0;
	   else {
		   float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
		   float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
		   float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
		   if (r == cmax)
		   hue = bluec - greenc;
		   else if (g == cmax)
			   hue = 2.0f + redc - bluec;
			   else
		   hue = 4.0f + greenc - redc;
		   hue = hue / 6.0f;
		   if (hue < 0)
		   hue = hue + 1.0f;
	   }
	   hsbvals[0] = hue;
	   hsbvals[1] = saturation;
	   hsbvals[2] = brightness;
	   return hsbvals;
	   }
	   
	float[] cacheRGB = new float[3];
	int cacheRGB_z = -1;
	
	public int red(float[] vv)
	{
		if (vv==cacheRGB) return red(cacheRGB_z);
		int z = HSBtoRGB(vv[0],vv[1],vv[2]);
		cacheRGB_z = z;
		cacheRGB = vv;
		return red(z);
	}
	public int green(float[] vv)
	{
		if (vv==cacheRGB) return green(cacheRGB_z);
		int z = HSBtoRGB(vv[0],vv[1],vv[2]);
		cacheRGB_z = z;
		cacheRGB = vv;
		return green(z);
	}
	public int blue(float[] vv)
	{
		if (vv==cacheRGB) return blue(cacheRGB_z);
		int z = HSBtoRGB(vv[0],vv[1],vv[2]);
		cacheRGB_z = z;
		cacheRGB = vv;
		return blue(z);
	}
	
	public static int HSBtoRGB(float hue, float saturation, float brightness) {
	int r = 0, g = 0, b = 0;
		if (saturation == 0) {
		r = g = b = (int) (brightness * 255.0f + 0.5f);
	} else {
		float h = (hue - (float)Math.floor(hue)) * 6.0f;
		float f = h - (float)java.lang.Math.floor(h);
		float p = brightness * (1.0f - saturation);
		float q = brightness * (1.0f - saturation * f);
		float t = brightness * (1.0f - (saturation * (1.0f - f)));
		switch ((int) h) {
		case 0:
		r = (int) (brightness * 255.0f + 0.5f);
		g = (int) (t * 255.0f + 0.5f);
		b = (int) (p * 255.0f + 0.5f);
		break;
		case 1:
		r = (int) (q * 255.0f + 0.5f);
		g = (int) (brightness * 255.0f + 0.5f);
		b = (int) (p * 255.0f + 0.5f);
		break;
		case 2:
		r = (int) (p * 255.0f + 0.5f);
		g = (int) (brightness * 255.0f + 0.5f);
		b = (int) (t * 255.0f + 0.5f);
		break;
		case 3:
		r = (int) (p * 255.0f + 0.5f);
		g = (int) (q * 255.0f + 0.5f);
		b = (int) (brightness * 255.0f + 0.5f);
		break;
		case 4:
		r = (int) (t * 255.0f + 0.5f);
		g = (int) (p * 255.0f + 0.5f);
		b = (int) (brightness * 255.0f + 0.5f);
		break;
		case 5:
		r = (int) (brightness * 255.0f + 0.5f);
		g = (int) (p * 255.0f + 0.5f);
		b = (int) (q * 255.0f + 0.5f);
		break;
		}
	}
	return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
	}
	
	
	public interface PixelProc
	{
		public void on(FrameBuffer f ,int x, int y);		
	}
	
	public void apply(PixelProc p)
	{
		for(int y=0;y<h;y++)
		{
			for(int x=0;x<w;x++)
			{
				p.on(this, x,y);
			}
		}
	}
	
}