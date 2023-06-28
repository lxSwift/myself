package com.xhgj.bigdata.util

/**
 * @Author luoxin
 * @Date 2023/6/28 9:46
 * @PackageName:com.xhgj.bigdata.util
 * @ClassName: AddressAnalysis
 * @Description: 地址获取省
 * @Version 1.0
 */
object AddressAnalysis {
  //根据地址值,来匹配他的省份(有些地址有省份有些只有市名称,所以需要一下参数处理)
  def provincesMatch(info: String) = {
    val provinces = List(
      "北京市", "天津市", "河北省", "山西省", "内蒙古自治区", "辽宁省", "吉林省",
      "黑龙江省", "上海市", "江苏省", "浙江省", "安徽省", "福建省", "江西省",
      "山东省", "河南省", "湖北省", "湖南省", "广东省", "广西壮族自治区", "海南省",
      "重庆市", "四川省", "贵州省", "云南省", "西藏自治区", "陕西省", "甘肃省",
      "青海省", "宁夏回族自治区", "新疆维吾尔自治区", "台湾省", "香港特别行政区", "澳门特别行政区"
    )
    val provinces_l = List("北京", "天津", "河北", "山西", "内蒙古", "辽宁", "吉林",
      "黑龙江", "上海", "江苏", "浙江", "安徽", "福建", "江西",
      "山东", "河南", "湖北", "湖南", "广东", "广西", "海南",
      "重庆", "四川", "贵州", "云南", "西藏", "陕西", "甘肃",
      "青海", "宁夏回族", "新疆", "台湾", "香港", "澳门")
    //中国所有地级市
    val cities = List( //河北省
      "石家庄", "唐山", "秦皇岛", "邯郸", "邢台", "保定", "张家口", "承德", "沧州", "廊坊", "衡水",
      //山西省
      "太原", "大同", "阳泉", "长治", "晋城", "朔州", "晋中", "运城", "忻州", "临汾", "吕梁",
      //内蒙古自治区
      "呼和浩特", "包头", "乌海", "赤峰", "通辽", "鄂尔多斯", "呼伦贝尔", "巴彦淖尔", "乌兰察布",
      //辽宁省
      "沈阳", "大连", "鞍山", "抚顺", "本溪", "丹东", "锦州", "营口", "阜新", "辽阳", "盘锦", "铁岭", "朝阳", "葫芦岛",
      //吉林省
      "长春", "吉林", "四平", "辽源", "通化", "白山", "松原", "白城",
      //黑龙江省
      "哈尔滨", "齐齐哈尔", "鸡西", "鹤岗", "双鸭山", "大庆", "伊春", "佳木斯", "七台河", "牡丹江", "黑河", "绥化",
      //江苏省
      "南京", "无锡", "徐州", "常州", "苏州", "南通", "连云港", "淮安", "盐城", "扬州", "镇江", "泰州", "宿迁",
      //浙江省
      "杭州", "宁波", "温州", "嘉兴", "湖州", "绍兴", "金华", "衢州", "舟山", "台州", "丽水",
      //安徽省
      "合肥", "芜湖", "蚌埠", "淮南", "马鞍山", "淮北", "铜陵", "安庆", "黄山", "阜阳", "宿州", "滁州", "六安", "宣城", "池州", "亳州",
      //福建省
      "福州", "厦门", "莆田", "三明", "泉州", "漳州", "南平", "龙岩", "宁德",
      //江西省
      "南昌", "景德镇", "萍乡", "九江", "抚州", "鹰潭", "赣州", "吉安", "宜春", "新余", "上饶",
      //山东省
      "济南", "青岛", "淄博", "枣庄", "东营", "烟台", "潍坊", "济宁", "泰安", "威海", "日照", "临沂", "德州", "聊城", "滨州", "菏泽",
      //河南省
      "郑州", "开封", "洛阳", "平顶山", "安阳", "鹤壁", "新乡", "焦作", "濮阳", "许昌", "漯河", "三门峡", "南阳", "商丘", "信阳", "周口", "驻马店",
      //湖北省
      "武汉", "黄石", "十堰", "宜昌", "襄阳", "鄂州", "荆门", "孝感", "荆州", "黄冈", "咸宁", "随州",
      //湖南省
      "长沙", "株洲", "湘潭", "衡阳", "邵阳", "岳阳", "常德", "张家界", "益阳", "郴州", "永州", "怀化", "娄底",
      //广东省
      "广州", "韶关", "深圳", "珠海", "汕头", "佛山", "江门", "湛江", "茂名", "肇庆", "惠州", "梅州", "汕尾", "河源", "阳江", "清远", "东莞", "中山", "潮州", "揭阳", "云浮",
      //广西壮族自治区
      "南宁", "柳州", "桂林", "梧州", "北海", "防城港", "钦州", "贵港", "玉林", "百色", "贺州", "河池", "来宾", "崇左",
      //海南省
      "海口", "三亚", "三沙", "儋州",
      //四川省
      "成都", "自贡", "攀枝花", "泸州", "德阳", "绵阳", "广元", "遂宁", "内江", "乐山", "南充", "眉山", "宜宾", "广安", "达州", "雅安", "巴中", "资阳",
      //贵州省
      "贵阳", "六盘水", "遵义", "安顺", "毕节", "铜仁",
      //云南省
      "昆明", "曲靖", "玉溪", "保山", "昭通", "丽江", "普洱", "临沧",
      //西藏自治区
      "拉萨", "日喀则", "昌都", "林芝", "山南", "那曲",
      //陕西省
      "西安", "铜川", "宝鸡", "咸阳", "渭南", "延安", "汉中", "榆林", "安康", "商洛",
      //甘肃省
      "兰州", "嘉峪关", "金昌", "白银", "天水", "武威", "张掖", "平凉", "酒泉", "庆阳", "定西", "陇南",
      //青海省
      "西宁", "海东",
      //宁夏回族自治区
      "银川", "石嘴山", "吴忠", "固原", "中卫",
      //新疆维吾尔自治区
      "乌鲁木齐", "克拉玛依", "吐鲁番", "哈密"
    )
    //先通过简称匹配到省份

    val matches = provinces_l.filter(province => info.contains(province))
    if (matches.isEmpty) {
      //如果省无法匹配到, 那就匹配市级
      val city_res = cities.filter(ct => info.contains(ct))
      if (city_res.isEmpty) {
        ""
      } else {
        val res = outputProvince(city_res(0))
        res
      }
    } else {
      val matches_str = matches(0)
      //标准化输出省份信息
      val end_pro = provinces.filter(province => province.contains(matches_str))(0)
      end_pro
    }
  }

  //根据输入的城市输出省份
  def outputProvince(city: String): String = {
    val provinceMap = Map(
      "河北省" -> List(
        "石家庄", "唐山", "秦皇岛", "邯郸", "邢台", "保定", "张家口", "承德", "沧州", "廊坊", "衡水"
      ),
      "山西省" -> List(
        "太原", "大同", "阳泉", "长治", "晋城", "朔州", "晋中", "运城", "忻州", "临汾", "吕梁"
      ),
      "内蒙古自治区" -> List(
        "呼和浩特", "包头", "乌海", "赤峰", "通辽", "鄂尔多斯", "呼伦贝尔", "巴彦淖尔", "乌兰察布"
      ),
      "辽宁省" -> List(
        "沈阳", "大连", "鞍山", "抚顺", "本溪", "丹东", "锦州", "营口", "阜新", "辽阳", "盘锦", "铁岭", "朝阳", "葫芦岛"
      ),
      "吉林省" -> List(
        "长春", "吉林", "四平", "辽源", "通化", "白山", "松原", "白城"
      ),
      "黑龙江省" -> List(
        "哈尔滨", "齐齐哈尔", "鸡西", "鹤岗", "双鸭山", "大庆", "伊春", "佳木斯", "七台河", "牡丹江", "黑河", "绥化"
      ),
      "江苏省" -> List(
        "南京", "无锡", "徐州", "常州", "苏州", "南通", "连云港", "淮安", "盐城", "扬州", "镇江", "泰州", "宿迁"
      ),
      "浙江省" -> List(
        "杭州", "宁波", "温州", "嘉兴", "湖州", "绍兴", "金华", "衢州", "舟山", "台州", "丽水"
      ),
      "安徽省" -> List(
        "合肥", "芜湖", "蚌埠", "淮南", "马鞍山", "淮北", "铜陵", "安庆", "黄山", "阜阳", "宿州", "滁州", "六安", "宣城", "池州", "亳州"
      ),
      "福建省" -> List(
        "福州", "厦门", "莆田", "三明", "泉州", "漳州", "南平", "龙岩", "宁德"
      ),
      "江西省" -> List(
        "南昌", "景德镇", "萍乡", "九江", "抚州", "鹰潭", "赣州", "吉安", "宜春", "新余", "上饶"
      ),
      "山东省" -> List(
        "济南", "青岛", "淄博", "枣庄", "东营", "烟台", "潍坊", "济宁", "泰安", "威海", "日照", "临沂", "德州", "聊城", "滨州", "菏泽"
      ),
      "河南省" -> List(
        "郑州", "开封", "洛阳", "平顶山", "安阳", "鹤壁", "新乡", "焦作", "濮阳", "许昌", "漯河", "三门峡", "南阳", "商丘", "信阳", "周口", "驻马店"),
      "湖北省" -> List(
        "武汉", "黄石", "十堰", "宜昌", "襄阳", "鄂州", "荆门", "孝感", "荆州", "黄冈", "咸宁", "随州"),
      "湖南省" -> List(
        "长沙", "株洲", "湘潭", "衡阳", "邵阳", "岳阳", "常德", "张家界", "益阳", "郴州", "永州", "怀化", "娄底"),
      "广东省" -> List(
        "广州", "韶关", "深圳", "珠海", "汕头", "佛山", "江门", "湛江", "茂名", "肇庆", "惠州", "梅州", "汕尾", "河源", "阳江", "清远", "东莞", "中山", "潮州", "揭阳", "云浮"),
      "广西壮族自治区" -> List(
        "南宁", "柳州", "桂林", "梧州", "北海", "防城港", "钦州", "贵港", "玉林", "百色", "贺州", "河池", "来宾", "崇左"),
      "海南省" -> List(
        "海口", "三亚", "三沙", "儋州"),
      "四川省" -> List(
        "成都", "自贡", "攀枝花", "泸州", "德阳", "绵阳", "广元", "遂宁", "内江", "乐山", "南充", "眉山", "宜宾", "广安", "达州", "雅安", "巴中", "资阳"),
      "贵州省" -> List(
        "贵阳", "六盘水", "遵义", "安顺", "毕节", "铜仁"),
      "云南省" -> List(
        "昆明", "曲靖", "玉溪", "保山", "昭通", "丽江", "普洱", "临沧"),
      "西藏自治区" -> List(
        "拉萨", "日喀则", "昌都", "林芝", "山南", "那曲"),
      "陕西省" -> List(
        "西安", "铜川", "宝鸡", "咸阳", "渭南", "延安", "汉中", "榆林", "安康", "商洛"),
      "甘肃省" -> List(
        "兰州", "嘉峪关", "金昌", "白银", "天水", "武威", "张掖", "平凉", "酒泉", "庆阳", "定西", "陇南"),
      "青海省" -> List(
        "西宁", "海东"),
      "宁夏回族自治区" -> List(
        "银川", "石嘴山", "吴忠", "固原", "中卫"),
      "新疆维吾尔自治区" -> List(
        "乌鲁木齐", "克拉玛依", "吐鲁番", "哈密"))

    val province = provinceMap.find { case (_, cityList) => cityList.contains(city) }

    val res: Option[String] = province match {
      case Some((provinceName, _)) => Some(provinceName)
      case None => None
    }
    res.getOrElse("")
  }

}
