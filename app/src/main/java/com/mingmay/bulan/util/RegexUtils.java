package com.mingmay.bulan.util;

import java.util.regex.Pattern;

public class RegexUtils 
{
	
	/**
	 * 验证是否是手机号码
	 * @param mobiles
	 * @return
	 */
	public static boolean isMobileNumber(String mobiles)
	{
		if(StringUtil.isBlank(mobiles))
		{
			return false;
		}
		return Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$").matcher(mobiles).matches();
	}
	
    //验证邮政编码  
    public static boolean checkPost(String post)
    {  
		if(StringUtil.isBlank(post))
		{
			return false;
		}
		return Pattern.compile("[1-9]\\d{5}(?!\\d)").matcher(post).matches();
    }
}
