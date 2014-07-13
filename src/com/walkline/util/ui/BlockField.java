package com.walkline.util.ui;

import com.walkline.util.Function;

import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;

public class BlockField extends Field
{
	private static Font _font; // = Font.getDefault().derive(Font.PLAIN, 30, Ui.UNITS_px);
	private static final int CORNER_RADIUS = 12;

	private int _value = 0;
	private int _fore_color = 0;
	private int _background_color = 0;
	private String _text = "";
	private int _size = 0;

	public BlockField()
	{
		super(NON_FOCUSABLE);

		try {
			FontFamily family = FontFamily.forName("Tahoma");
			_font = family.getFont(Font.EXTRA_BOLD, 8, Ui.UNITS_pt);
		} catch (ClassNotFoundException e) {}

		//int totalSize = Math.min(Display.getWidth(), Display.getHeight()) - 20 - 32;

		//_size = (totalSize - 50) / 4;
		_value = 0;
		_background_color = Color.GRAY;
		_fore_color = Color.WHITE;
	}

	public int getValue() {return _value;}

	public void setValue(int value)
	{
		_value = value;
		_text = ((value != 0) ? String.valueOf(value) : ""); 

		_fore_color = ((_value > 4) ? 0xf9f6f2 : 0x776e65);
		setBackgroundColor();
	}

	public void setBackgroundColor()
	{
		switch (_value)
		{
			case 0:
				_background_color = 0xccc0b3;
				break;
			case 2:
				_background_color = 0xeee4da;
				break;
			case 4:
				_background_color = 0xeee0c8;
				break;
			case 8:
				_background_color = 0xf2b179;
				break;
			case 16:
				_background_color = 0xf59563;
				break;
			case 32:
				_background_color = 0xf67c5f;
				break;
			case 64: // not modified
				_background_color = 0xf95e32; 
				break;
			case 128:
				_background_color = 0xefcf6c;
				break;
			case 256: 
				_background_color = 0xefcf63;
				break;
			case 512:
				_background_color = 0xefcb52;
				break;
			case 1024:
				_background_color = 0xefc739;
				break;
			case 2048:
				_background_color = 0xefc329;
				break;
			case 4096:
				_background_color = 0xff3c39;
				break;
		}
	}

	//public int getPreferredWidth() {return _size;}

	//public int getPreferredHeight() {return _size;}

	protected void layout(int width, int height) {setExtent(width, height);}

	protected void paint(Graphics g)
	{
		g.setColor(_fore_color);
		g.setFont(_font);
		g.drawText(_text, (getWidth() - _font.getAdvance(_text)) / 2, (getHeight() - _font.getHeight()) / 2);
	}

	protected void paintBackground(Graphics g)
	{
		g.setColor(_background_color);
		g.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
	}
}