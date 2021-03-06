package com.walkline.screen;

import java.util.Random;
import localization.Game2048Resource;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import com.walkline.app.Game2048AppConfig;
import com.walkline.util.Function;
import com.walkline.util.Enumerations.GameModes;
import com.walkline.util.ui.BlockField;
import com.walkline.util.ui.BlockFieldManager;
import com.walkline.util.ui.ForegroundManager;
import com.walkline.util.ui.ScoreboardFieldManager;

public class Game2048Screen extends MainScreen implements Game2048Resource
{
	private boolean IS_WIDTH_SCREEN = Display.getWidth() > Display.getHeight() ? true : false;

	private static ResourceBundle _bundle = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	private Game2048AppConfig _appConfig;
	private int LINES = Game2048AppConfig.LINES;
	private ForegroundManager _foreground = new ForegroundManager(0);
	private BlockField[][] _block = new BlockField[LINES][LINES];
	private BlockFieldManager _mainFrame;
	private ScoreboardFieldManager _scoreBoard;
	private static int _lastMovementTime = 0;
	private static float _startX;
	private static float _startY;
	private static float _offsetX;
	private static float _offsetY;

    public Game2048Screen(Game2048AppConfig appConfig)
    {
    	super(NO_HORIZONTAL_SCROLL | NO_VERTICAL_SCROLL | NO_SYSTEM_MENU_ITEMS);
    	setDefaultClose(false);

    	_appConfig = appConfig;
    	_mainFrame = new BlockFieldManager(IS_WIDTH_SCREEN ? Field.FIELD_LEFT : Field.FIELD_HCENTER);
    	_scoreBoard = new ScoreboardFieldManager(IS_WIDTH_SCREEN ? Field.FIELD_RIGHT : Field.FIELD_HCENTER);
    	_scoreBoard.setBestScore(_appConfig.getBestScore());

    	if (IS_WIDTH_SCREEN)
    	{
    		HorizontalFieldManager hfm = new HorizontalFieldManager(USE_ALL_WIDTH);
        	hfm.add(_mainFrame);
        	hfm.add(_scoreBoard);
        	_foreground.add(hfm);
    	} else {
    		VerticalFieldManager vfm = new VerticalFieldManager(USE_ALL_WIDTH);
    		vfm.add(_scoreBoard);
    		vfm.add(_mainFrame);
    		_foreground.add(vfm);
    	}

        add(_foreground);

        UiApplication.getUiApplication().invokeLater(new Runnable()
        {
			public void run() {startGame();}
		});
    }

    private void startGame()
    {
//    	ChooseGameModeScreen _uploadScreen = new ChooseGameModeScreen();
//		UiApplication.getUiApplication().pushModalScreen(_uploadScreen);
//
//		int selection = _uploadScreen.getSelection();
//
//		if (selection == -1) {showExitDialog();}
//
//		switch (selection)
//		{
//			case GameModes.EASY:
//				LINES = 5;
//				break;
//			case GameModes.NORMAL:
//				LINES = 4;
//				break;
//			case GameModes.HARD:
//				LINES = 3;
//				break;
//		}

    	int selection = GameModes.NORMAL;

		_mainFrame.setGameMode(selection);
		_scoreBoard.setGameMode(selection);
		_block = new BlockField[LINES][LINES];
		for (int x=0; x<LINES; x++)
        {
        	for (int y=0; y<LINES; y++)
        	{
            	_block[x][y] = new BlockField(selection);
            	_block[x][y].setAnimationMode(false);
            	_block[x][y].Clear();
            	_mainFrame.add(_block[x][y]);
        	}
        }

		initGame();
    }

    private void initGame()
    {
    	_scoreBoard.clear();

    	for (int x=0; x<LINES; x++)
    	{
    		for (int y=0; y<LINES; y++)
    		{
    			_block[x][y].setAnimationMode(false);
    			_block[x][y].Clear();
    		}
    	}

    	_mainFrame.invalidate();
    	appendBlock();
    	appendBlock();
    }

	private void appendBlock()
	{
		int x = 0;
		int y = 0;
		boolean found = false;

		if (_mainFrame.DisplayCount() >= (LINES * LINES)) {return;}

		while (!found)
		{
			x = new Random(System.currentTimeMillis() * System.currentTimeMillis()).nextInt(LINES);
			y = new Random(System.currentTimeMillis() * System.currentTimeMillis()).nextInt(LINES);

			if (_block[x][y].getValue() == 0)
			{
				if (new Random(System.currentTimeMillis() * System.currentTimeMillis()).nextFloat() > 0.3)
				{
					_block[x][y].setValue(2);
				} else {
					_block[x][y].setValue(4);
				}

				found = true;
				_block[x][y].setAnimationMode(true);
				_block[x][y].startAnimation();
			}
		}
	}

	private void updateScore(int value) {_scoreBoard.update(value);}

	private void moveBlockUp()
	{
		boolean merge = false;

		for (int y=0; y<LINES; y++)
		{
			for (int x=0; x<LINES; x++)
			{
				for (int x1=x+1; x1<LINES; x1++)
				{
					if (_block[x1][y].getValue() > 0)
					{
						if (_block[x][y].getValue() <= 0)
						{
							_block[x][y].setValue(_block[x1][y].getValue());
							_block[x1][y].setValue(0);

							x--;
							merge = true;
						} else if (_block[x][y].equals(_block[x1][y])) {
							_block[x][y].setValue(_block[x][y].getValue() * 2);
							_block[x][y].setAnimationMode(true);
							_block[x][y].startAnimation();
							_block[x1][y].setValue(0);

							updateScore(_block[x][y].getValue());
							merge = true;
						}

						break;
					}
				}
			}
		}

		if (merge) {
			appendBlock();
			checkComplete();
		}

		_mainFrame.invalidate();
	}

	private void moveBlockDown()
	{
		boolean merge = false;

		for (int y=0; y<LINES; y++)
		{
			for (int x=LINES-1; x>=0; x--)
			{
				for (int x1=x-1; x1>=0; x1--)
				{
					if (_block[x1][y].getValue() > 0)
					{
						if (_block[x][y].getValue() <= 0)
						{
							//MainActivity.getMainActivity().getAnimLayer().createMoveAnim(_block[x1][y], _block[x][y],x1, x, y, y);
							_block[x][y].setValue(_block[x1][y].getValue());
							_block[x1][y].setValue(0);

							x++;
							merge = true;
						} else if (_block[x][y].equals(_block[x1][y])) {
							//MainActivity.getMainActivity().getAnimLayer().createMoveAnim(_block[x1][y], _block[x][y],x1, x, y, y);
							_block[x][y].setValue(_block[x][y].getValue() * 2);
							_block[x][y].setAnimationMode(true);
							_block[x][y].startAnimation();
							_block[x1][y].setValue(0);

							updateScore(_block[x][y].getValue());
							merge = true;
						}

						break;
					}
				}
			}
		}

		if (merge) {
			appendBlock();
			checkComplete();
		}

		_mainFrame.invalidate();
	}

	private void moveBlockLeft()
	{
		boolean merge = false;

		for (int x=0; x<LINES; x++)
		{
			for (int y=0; y<LINES; y++)
			{
				for (int y1=y+1; y1<LINES; y1++)
				{
					if (_block[x][y1].getValue() > 0)
					{
						if (_block[x][y].getValue() <= 0)
						{
							//MainActivity.getMainActivity().getAnimLayer().createMoveAnim(_block[x][y1],_block[x][y], x, x, y1, y);
							_block[x][y].setValue(_block[x][y1].getValue());
							_block[x][y1].setValue(0);

							y--;
							merge = true;
						} else if (_block[x][y].equals(_block[x][y1])) {
							//MainActivity.getMainActivity().getAnimLayer().createMoveAnim(_block[x][y1],_block[x][y], x, x, y1, y);
							_block[x][y].setValue(_block[x][y].getValue() * 2);
							_block[x][y].setAnimationMode(true);
							_block[x][y].startAnimation();
							_block[x][y1].setValue(0);

							updateScore(_block[x][y].getValue());
							merge = true;
						}

						break;
					}
				}
			}
		}

		if (merge) {
			appendBlock();
			checkComplete();
		}

		_mainFrame.invalidate();
	}

	private void moveBlockRight()
	{
		boolean merge = false;

		for (int x=0; x<LINES; x++)
		{
			for (int y=LINES-1; y>=0; y--)
			{
				for (int y1=y-1; y1>=0; y1--)
				{
					if (_block[x][y1].getValue()>0)
					{
						if (_block[x][y].getValue() <= 0)
						{
							//MainActivity.getMainActivity().getAnimLayer().createMoveAnim(_block[x][y1],_block[x][y], x, x, y1, y);
							_block[x][y].setValue(_block[x][y1].getValue());
							_block[x][y1].setValue(0);

							y++;
							merge = true;
						} else if (_block[x][y].equals(_block[x][y1])) {
							//MainActivity.getMainActivity().getAnimLayer().createMoveAnim(_block[x][y1],_block[x][y], x, x, y1, y);
							_block[x][y].setValue(_block[x][y].getValue() * 2);
							_block[x][y].setAnimationMode(true);
							_block[x][y].startAnimation();
							_block[x][y1].setValue(0);

							updateScore(_block[x][y].getValue());
							merge = true;
						}

						break;
					}
				}
			}
		}

		if (merge) {
			appendBlock();
			checkComplete();
		}

		_mainFrame.invalidate();
	}

	private void checkComplete()
	{
		boolean complete = true;

ALL:
		for (int y=0; y<LINES; y++)
		{
			for (int x=0; x<LINES; x++)
			{
				if (_block[x][y].getValue() == 0 ||
				   (x > 0 && _block[x][y].equals(_block[x-1][y])) ||
				   (x < LINES - 1 && _block[x][y].equals(_block[x+1][y])) ||
				   (y > 0 && _block[x][y].equals(_block[x][y-1])) ||
				   (y < LINES - 1 && _block[x][y].equals(_block[x][y+1]))) {
						complete = false;
						break ALL;
				}
			}
		}

		if (complete)
		{
			if (_scoreBoard.getBestScore() > _appConfig.getBestScore())
			{
				_appConfig.setBestScore(_scoreBoard.getBestScore());
				_appConfig.save();
			}

			UiApplication.getUiApplication().invokeLater(new Runnable()
			{
				public void run() {showRestartDialog();}
			});
		}
	}

	private void showRestartDialog()
	{
		String[] choices = getResStringArray(DIALOG_CHOICES_OKRESTART);

		Dialog restartDialog = new Dialog(getResString(DIALOG_MESSAGE_GAMEOVER), choices, null, 1, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION));

		restartDialog.doModal();
		if (restartDialog.getSelectedValue() == 1) {initGame();}
	}

    private void showExitDialog()
    {
		String[] yesno = getResStringArray(DIALOG_CHOICES_YESNO);
		Dialog showDialog = new Dialog(getResString(DIALOG_MESSAGE_QUIT), yesno, null, 1, Bitmap.getPredefinedBitmap(Bitmap.QUESTION), USE_ALL_WIDTH);

		showDialog.doModal();
		if (showDialog.getSelectedValue() == 0) {System.exit(0);}
    }

    private void showAllBlocks()
    {
    	//_block[0][0].setAnimationMode(false);
    	//_block[0][0].Clear();
    	_block[0][0].setValue(2);
    	_block[0][1].setValue(8192);
    	_block[0][2].setValue(4096);
    	_block[1][0].setValue(2048);
    	_block[1][1].setValue(1024);
    	_block[1][2].setValue(512);
    	_block[2][0].setValue(256);
    	_block[2][1].setValue(128);
    	_block[2][2].setValue(64);

    	_mainFrame.invalidate();
    }

	private String getResString(int key) {return _bundle.getString(key);}
	private String[] getResStringArray(int key) {return _bundle.getStringArray(key);}

    public boolean onClose()
    {
    	UiApplication.getUiApplication().requestBackground();

    	return true;
    }

	protected boolean keyChar(char key, int status, int time)
	{
		switch (key)
		{
			case Characters.LATIN_CAPITAL_LETTER_L:
			case Characters.LATIN_SMALL_LETTER_L:
				UiApplication.getUiApplication().pushScreen(new GameRankingScreen());
				return true;
			case Characters.LATIN_CAPITAL_LETTER_R:
			case Characters.LATIN_SMALL_LETTER_R:
				initGame();
				return true;
			case Characters.LATIN_CAPITAL_LETTER_A:
			case Characters.LATIN_SMALL_LETTER_A:
				appendBlock();
				return true;
			case Characters.LATIN_CAPITAL_LETTER_C:
			case Characters.LATIN_SMALL_LETTER_C:
				Function.errorDialog(_mainFrame.DisplayCount() + "");
				return true;
			case Characters.LATIN_CAPITAL_LETTER_Q:
			case Characters.LATIN_SMALL_LETTER_Q:
				showExitDialog();
				return true;
			case Characters.LATIN_CAPITAL_LETTER_P:
			case Characters.LATIN_SMALL_LETTER_P:
				showAllBlocks();
				return true;
		}

		return super.keyChar(key, status, time);
	}

    protected boolean navigationMovement(int dx, int dy, int status, int time)
    {
    	if (Math.abs(dx) > Math.abs(dy))
    	{
    		if (time - _lastMovementTime > 300)
    		{
    			if (dx < 0)
    			{
    				moveBlockLeft();
    			} else if (dx > 0) {
    				moveBlockRight();
    			}

    			_lastMovementTime = time;
    			return true;
    		}
    	} else {
    		if (time - _lastMovementTime > 300)
    		{
    			if (dy < 0)
    			{
    				moveBlockUp();
    			} else if (dy > 0) {
    				moveBlockDown();
    			}

    			_lastMovementTime = time;
    			return true;
    		}
    	}

    	return super.navigationMovement(dx, dy, status, time);
    }

    protected boolean trackwheelRoll(int amount, int status, int time)
    {
    	return super.trackwheelRoll(amount, status, time);
    }

    protected boolean touchEvent(TouchEvent message)
    {
    	int event = message.getEvent();

    	switch (event)
    	{
    		case TouchEvent.DOWN:
    			_startX = message.getX(1);
    			_startY = message.getY(1);
    			break;
    		case TouchEvent.UP:
    			_offsetX = message.getX(1) - _startX;
    			_offsetY = message.getY(1) - _startY;

    			if (Math.abs(_offsetX) > Math.abs(_offsetY))
    			{
    				if (_offsetX < -5)
    				{
    					moveBlockLeft();
    				} else if (_offsetX > 5) {
    					moveBlockRight();
    				}
    			} else {
    				if (_offsetY < -5)
    				{
    					moveBlockUp();
    				} else if (_offsetY > 5) {
    					moveBlockDown();
    				}
    			}
    			break;
		}

    	return true;
    }

    MenuItem menuRestart = new MenuItem(_bundle, MENU_RESTART, 100, 10)
    {
    	public void run() {initGame();}
    };

    MenuItem menuRanking = new MenuItem(_bundle, MENU_RANKING, 100, 20)
    {
    	public void run() {UiApplication.getUiApplication().pushScreen(new GameRankingScreen());}
    };

    MenuItem menuUploadScore = new MenuItem(_bundle, MENU_UPLOADSCORE, 100, 30)
    {
    	public void run() {UiApplication.getUiApplication().pushScreen(new UploadScoreScreen(_appConfig));}
    };

    MenuItem menuChooseGameMode = new MenuItem(_bundle, MENU_GAMEMODE, 100, 40)
    {
		public void run()
		{
			ChooseGameModeScreen popupScreen = new ChooseGameModeScreen();
			UiApplication.getUiApplication().pushModalScreen(popupScreen);

			int selection = popupScreen.getSelection();

			if (selection == -1) {return;}

			switch (selection)
			{
				case GameModes.EASY:
					LINES = 5;
					break;
				case GameModes.NORMAL:
					LINES = 4;
					break;
				case GameModes.HARD:
					LINES = 3;
					break;
			}

			_mainFrame.deleteAll();
			_mainFrame.setGameMode(selection);
			_scoreBoard.setGameMode(selection);
			_block = new BlockField[LINES][LINES];

			for (int x=0; x<LINES; x++)
	        {
	        	for (int y=0; y<LINES; y++)
	        	{
	            	_block[x][y] = new BlockField(selection);
	            	_block[x][y].setAnimationMode(false);
	            	_block[x][y].Clear();
	            	_mainFrame.add(_block[x][y]);
	        	}
	        }

			initGame();
		}
	};

    MenuItem menuExit = new MenuItem(_bundle, MENU_EXIT, 100, 50)
    {
    	public void run() {showExitDialog();}
    };

    protected void makeMenu(Menu menu, int instance)
    {
    	menu.add(menuRestart);
    	menu.addSeparator();
    	menu.add(menuRanking);
    	menu.addSeparator();
    	//menu.add(menuChooseGameMode);
    	menu.add(menuUploadScore);
    	menu.addSeparator();
    	menu.add(menuExit);

    	super.makeMenu(menu, instance);
    }
}