# AI推荐算法方案

## 秘境探索 (HiddenGems) - 智能推荐系统设计

---

## 1. 系统概览

### 1.1 推荐系统架构

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        AI推荐系统架构                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   用户交互层                                                             │
│   ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐         │
│   │ 首页推荐 │ │ 搜索结果 │ │ 详情关联 │ │ 行程生成 │ │ 智能问答 │         │
│   └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘         │
│        │           │           │           │           │                │
│        └───────────┴───────────┴─────┬─────┴───────────┘                │
│                                      │                                  │
│   ┌──────────────────────────────────▼──────────────────────────────┐  │
│   │                      推荐引擎 (Recommendation Engine)            │  │
│   │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │  │
│   │  │ 协同过滤    │ │ 内容推荐    │ │ 混合推荐    │               │  │
│   │  │ (CF)       │ │ (CB)        │ │ (Hybrid)    │               │  │
│   │  └─────────────┘ └─────────────┘ └─────────────┘               │  │
│   └──────────────────────────────────────────────────────────────────┘  │
│                                      │                                  │
│   ┌──────────────────────────────────▼──────────────────────────────┐  │
│   │                      AI增强层 (AI Enhancement)                   │  │
│   │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │  │
│   │  │ LLM理解    │ │ 向量嵌入    │ │ 上下文感知  │               │  │
│   │  │ (GPT-4)    │ │ (Embedding) │ │ (Context)   │               │  │
│   │  └─────────────┘ └─────────────┘ └─────────────┘               │  │
│   └──────────────────────────────────────────────────────────────────┘  │
│                                      │                                  │
│   ┌──────────────────────────────────▼──────────────────────────────┐  │
│   │                      数据层 (Data Layer)                         │  │
│   │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │  │
│   │  │ 用户画像    │ │ 景点特征    │ │ 行为日志    │               │  │
│   │  │ (User)      │ │ (Spot)      │ │ (Behavior)  │               │  │
│   │  └─────────────┘ └─────────────┘ └─────────────┘               │  │
│   │  ┌─────────────┐ ┌─────────────┐                               │  │
│   │  │ 向量数据库  │ │ 知识图谱    │                               │  │
│   │  │ (Pinecone) │ │ (Neo4j)     │                               │  │
│   │  └─────────────┘ └─────────────┘                               │  │
│   └──────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.2 推荐场景

| 场景 | 输入 | 输出 | 算法 |
|------|------|------|------|
| 首页推荐 | 用户画像 + 位置 | Top N景点 | 混合推荐 + LLM |
| 搜索 | 关键词 + 筛选条件 | 排序结果 | 语义搜索 + BM25 |
| 详情页关联 | 当前景点 | 相关景点 | 内容相似度 + 图谱 |
| 行程生成 | 需求参数 | 完整行程 | LLM + 约束优化 |
| 智能问答 | 问题 + 上下文 | 回答 + 景点 | RAG + LLM |

---

## 2. 用户画像系统

### 2.1 用户特征提取

```python
class UserProfile:
    """用户画像模型"""

    # 基础属性
    user_id: str
    age_range: str          # 年龄段
    gender: str             # 性别
    location: GeoLocation   # 常居地

    # 偏好标签 (显式 + 隐式)
    preference_tags: Dict[str, float]  # {"自然": 0.9, "摄影": 0.7, ...}

    # 行为统计
    stats: UserStats

    # 向量嵌入
    embedding: List[float]  # 768维用户向量

@dataclass
class UserStats:
    """用户行为统计"""
    total_spots_viewed: int       # 浏览景点数
    total_spots_visited: int      # 打卡景点数
    total_reviews: int            # 评价数
    favorite_categories: List     # 偏好分类
    avg_trip_duration: float      # 平均行程天数
    budget_preference: str        # 预算偏好
    crowd_preference: str         # 人流偏好
```

### 2.2 偏好学习算法

```python
class PreferenceLearner:
    """用户偏好学习器"""

    def learn_from_behavior(self, user_id: str, behaviors: List[Behavior]) -> Dict[str, float]:
        """
        从用户行为学习偏好

        行为权重:
        - 查看详情: 1.0
        - 收藏: 3.0
        - 加入行程: 5.0
        - 实际到访: 10.0
        - 写评价: 8.0
        - 分享: 6.0
        """
        tag_scores = defaultdict(float)

        for behavior in behaviors:
            spot = get_spot(behavior.spot_id)
            weight = BEHAVIOR_WEIGHTS[behavior.action]

            for tag in spot.tags:
                tag_scores[tag] += weight

            # 时间衰减 (近期行为权重更高)
            decay = self._time_decay(behavior.timestamp)
            tag_scores[tag] *= decay

        # 归一化
        max_score = max(tag_scores.values())
        return {tag: score / max_score for tag, score in tag_scores.items()}

    def _time_decay(self, timestamp: datetime) -> float:
        """时间衰减函数"""
        days_ago = (datetime.now() - timestamp).days
        return math.exp(-days_ago / 30)  # 30天半衰期
```

### 2.3 冷启动策略

```python
class ColdStartHandler:
    """冷启动处理"""

    def handle_new_user(self, user: User) -> UserProfile:
        """
        新用户冷启动策略
        """
        # 1. 引导问卷 (可选)
        if user.completed_onboarding:
            preferences = self._extract_from_onboarding(user.onboarding_answers)
        else:
            # 2. 基于位置推荐
            preferences = self._location_based_preferences(user.location)

        # 3. 默认偏好
        default_prefs = {
            "自然风光": 0.5,
            "人文古迹": 0.5,
            "美食探店": 0.5,
        }

        # 合并
        final_prefs = {**default_prefs, **preferences}

        return UserProfile(
            user_id=user.id,
            preference_tags=final_prefs,
            embedding=self._create_initial_embedding(final_prefs)
        )

    def _location_based_preferences(self, location: GeoLocation) -> Dict[str, float]:
        """基于位置推断偏好"""
        # 获取附近热门景点，统计标签分布
        nearby_spots = get_nearby_spots(location, radius=50)
        tag_counts = Counter(tag for spot in nearby_spots for tag in spot.tags)

        # 归一化
        total = sum(tag_counts.values())
        return {tag: count / total for tag, count in tag_counts.most_common(10)}
```

---

## 3. 景点特征系统

### 3.1 景点特征提取

```python
class SpotFeatures:
    """景点特征模型"""

    spot_id: str

    # 基础特征
    name: str
    description: str
    category: str
    tags: List[str]

    # 位置特征
    location: GeoLocation
    country: str
    province: str
    city: str

    # 统计特征
    rating: float
    review_count: int
    view_count: int
    favorite_count: int

    # 动态特征
    crowd_level: str          # 当前人流
    seasonal_score: float     # 季节适合度
    weather_score: float      # 天气适合度

    # 内容嵌入
    text_embedding: List[float]   # 文本向量 (768维)
    image_embedding: List[float]  # 图像向量 (512维)

    # 图谱特征
    connected_spots: List[str]    # 相关联景点
    themes: List[str]             # 主题分类
```

### 3.2 文本嵌入生成

```python
class SpotEmbeddingGenerator:
    """景点嵌入向量生成"""

    def __init__(self):
        self.embedding_model = "text-embedding-3-small"
        self.client = OpenAI()

    def generate_embedding(self, spot: Spot) -> List[float]:
        """
        生成景点文本嵌入

        组合文本:
        - 景点名称
        - 分类
        - 标签
        - AI简介
        - 位置信息
        """
        text_parts = [
            f"景点: {spot.name}",
            f"分类: {spot.category}",
            f"标签: {', '.join(spot.tags)}",
            f"位置: {spot.province} {spot.city}",
            f"简介: {spot.ai_summary or spot.description[:200]}",
        ]

        combined_text = "\n".join(text_parts)

        response = self.client.embeddings.create(
            model=self.embedding_model,
            input=combined_text
        )

        return response.data[0].embedding

    def batch_generate(self, spots: List[Spot]) -> Dict[str, List[float]]:
        """批量生成嵌入"""
        embeddings = {}
        for spot in spots:
            embeddings[spot.id] = self.generate_embedding(spot)
        return embeddings
```

### 3.3 景点相似度计算

```python
class SpotSimilarityCalculator:
    """景点相似度计算"""

    def calculate_similarity(self, spot1: Spot, spot2: Spot) -> float:
        """
        综合相似度计算

        = 0.4 * 文本相似度
        + 0.2 * 标签相似度
        + 0.2 * 位置相似度
        + 0.1 * 评分相似度
        + 0.1 * 主题相似度
        """
        text_sim = self._cosine_similarity(
            spot1.text_embedding,
            spot2.text_embedding
        )

        tag_sim = self._jaccard_similarity(spot1.tags, spot2.tags)

        location_sim = self._location_similarity(
            spot1.location, spot2.location
        )

        rating_sim = 1 - abs(spot1.rating - spot2.rating) / 5

        theme_sim = self._jaccard_similarity(spot1.themes, spot2.themes)

        return (
            0.4 * text_sim +
            0.2 * tag_sim +
            0.2 * location_sim +
            0.1 * rating_sim +
            0.1 * theme_sim
        )

    def _cosine_similarity(self, vec1: List[float], vec2: List[float]) -> float:
        """余弦相似度"""
        return np.dot(vec1, vec2) / (np.linalg.norm(vec1) * np.linalg.norm(vec2))

    def _jaccard_similarity(self, set1: List, set2: List) -> float:
        """Jaccard相似度"""
        s1, s2 = set(set1), set(set2)
        return len(s1 & s2) / len(s1 | s2) if s1 | s2 else 0

    def _location_similarity(self, loc1: GeoLocation, loc2: GeoLocation) -> float:
        """位置相似度 (距离衰减)"""
        distance = haversine_distance(loc1, loc2)  # km
        return math.exp(-distance / 50)  # 50km半衰
```

---

## 4. 推荐算法

### 4.1 混合推荐算法

```python
class HybridRecommender:
    """混合推荐引擎"""

    def __init__(self):
        self.cf_recommender = CollaborativeFilteringRecommender()
        self.cb_recommender = ContentBasedRecommender()
        self.context_filter = ContextFilter()

    def recommend(
        self,
        user: UserProfile,
        context: RecommendationContext,
        k: int = 10
    ) -> List[Recommendation]:
        """
        混合推荐主流程

        1. 协同过滤召回 (召回阶段)
        2. 内容推荐召回 (召回阶段)
        3. 合并去重
        4. 上下文过滤 (过滤阶段)
        5. 多样性重排 (重排阶段)
        6. LLM增强 (增强阶段)
        """
        # 1. 协同过滤召回
        cf_candidates = self.cf_recommender.recall(user, k=50)

        # 2. 内容推荐召回
        cb_candidates = self.cb_recommender.recall(user, k=50)

        # 3. 合并去重
        candidates = self._merge_candidates(cf_candidates, cb_candidates)

        # 4. 上下文过滤
        filtered = self.context_filter.filter(candidates, context)

        # 5. 排序
        ranked = self._rank(filtered, user, context)

        # 6. 多样性重排
        diversified = self._diversify(ranked, k=k*2)

        # 7. LLM增强
        enhanced = self._llm_enhance(diversified[:k], user, context)

        return enhanced

    def _diversify(self, items: List[Recommendation], k: int) -> List[Recommendation]:
        """
        多样性重排 (MMR算法)

        平衡相关性和多样性
        """
        selected = []
        remaining = items.copy()

        while len(selected) < k and remaining:
            max_mmr = -float('inf')
            best_item = None
            best_idx = 0

            for idx, item in enumerate(remaining):
                # MMR = λ * relevance - (1-λ) * max_similarity_to_selected
                relevance = item.score

                if selected:
                    max_sim = max(
                        self._item_similarity(item, s)
                        for s in selected
                    )
                else:
                    max_sim = 0

                mmr = 0.7 * relevance - 0.3 * max_sim

                if mmr > max_mmr:
                    max_mmr = mmr
                    best_item = item
                    best_idx = idx

            selected.append(best_item)
            remaining.pop(best_idx)

        return selected
```

### 4.2 协同过滤推荐

```python
class CollaborativeFilteringRecommender:
    """协同过滤推荐器"""

    def recall(self, user: UserProfile, k: int = 50) -> List[Recommendation]:
        """
        基于用户的协同过滤 (User-CF)

        找到相似用户喜欢的景点
        """
        # 1. 找到相似用户
        similar_users = self._find_similar_users(user, k=100)

        # 2. 获取相似用户喜欢的景点
        candidate_spots = defaultdict(float)

        for similar_user, similarity in similar_users:
            user_favorites = get_user_favorites(similar_user.user_id)

            for spot_id in user_favorites:
                # 加权评分
                candidate_spots[spot_id] += similarity * 1.0

        # 3. 过滤已知的景点
        known_spots = get_user_known_spots(user.user_id)
        candidate_spots = {
            k: v for k, v in candidate_spots.items()
            if k not in known_spots
        }

        # 4. 排序返回
        sorted_spots = sorted(
            candidate_spots.items(),
            key=lambda x: x[1],
            reverse=True
        )[:k]

        return [
            Recommendation(spot_id=spot_id, score=score, source="cf")
            for spot_id, score in sorted_spots
        ]

    def _find_similar_users(
        self,
        user: UserProfile,
        k: int = 100
    ) -> List[Tuple[UserProfile, float]]:
        """找到相似用户 (基于用户向量)"""
        # 向量检索
        similar = vector_db.query(
            collection="user_embeddings",
            vector=user.embedding,
            top_k=k
        )

        return similar
```

### 4.3 内容推荐

```python
class ContentBasedRecommender:
    """基于内容的推荐器"""

    def recall(self, user: UserProfile, k: int = 50) -> List[Recommendation]:
        """
        基于用户偏好向量匹配景点

        使用向量检索加速
        """
        # 1. 构建用户查询向量
        query_vector = self._build_query_vector(user)

        # 2. 向量检索
        results = vector_db.query(
            collection="spot_embeddings",
            vector=query_vector,
            top_k=k * 2,  # 多召回一些，后面过滤
            filter={
                "status": "active",
                "crowd_level": {"$ne": "high"} if user.crowd_preference == "avoid" else None
            }
        )

        # 3. 过滤已知的景点
        known_spots = get_user_known_spots(user.user_id)

        recommendations = []
        for spot_id, similarity in results:
            if spot_id not in known_spots:
                recommendations.append(
                    Recommendation(
                        spot_id=spot_id,
                        score=similarity,
                        source="content"
                    )
                )

        return recommendations[:k]

    def _build_query_vector(self, user: UserProfile) -> List[float]:
        """构建查询向量"""
        # 方案1: 直接使用用户嵌入
        if user.embedding:
            return user.embedding

        # 方案2: 基于偏好标签构建
        tag_embeddings = []
        for tag, weight in user.preference_tags.items():
            tag_vec = get_tag_embedding(tag)
            tag_embeddings.append((tag_vec, weight))

        # 加权平均
        query_vector = np.zeros(768)
        for vec, weight in tag_embeddings:
            query_vector += np.array(vec) * weight

        return query_vector.tolist()
```

### 4.4 上下文过滤

```python
class ContextFilter:
    """上下文过滤器"""

    def filter(
        self,
        candidates: List[Recommendation],
        context: RecommendationContext
    ) -> List[Recommendation]:
        """
        根据上下文过滤候选景点
        """
        filtered = []

        for rec in candidates:
            spot = get_spot(rec.spot_id)

            # 1. 距离过滤
            if context.max_distance:
                distance = calculate_distance(context.location, spot.location)
                if distance > context.max_distance:
                    continue

            # 2. 人流过滤
            if context.crowd_preference == "avoid":
                if spot.crowd_level == "high":
                    continue

            # 3. 分类过滤
            if context.categories:
                if spot.category not in context.categories:
                    continue

            # 4. 标签过滤
            if context.required_tags:
                if not set(context.required_tags) & set(spot.tags):
                    continue

            # 5. 季节适合度
            if context.current_season:
                season_score = self._calculate_season_score(spot, context.current_season)
                if season_score < 0.5:
                    rec.score *= 0.7  # 降低分数但不完全排除

            filtered.append(rec)

        return filtered

    def _calculate_season_score(self, spot: Spot, season: str) -> float:
        """计算季节适合度"""
        if season in spot.best_seasons:
            return 1.0
        elif not spot.best_seasons:
            return 0.8  # 无季节限制
        else:
            return 0.3  # 不适合当前季节
```

### 4.5 LLM增强层

```python
class LLMEnhancer:
    """LLM增强层"""

    def __init__(self):
        self.client = OpenAI()
        self.model = "gpt-4-turbo"

    def enhance_recommendations(
        self,
        recommendations: List[Recommendation],
        user: UserProfile,
        context: RecommendationContext
    ) -> List[Recommendation]:
        """
        使用LLM增强推荐结果

        1. 生成个性化推荐理由
        2. 调整排序
        3. 添加小贴士
        """
        # 获取景点详情
        spots = [get_spot(r.spot_id) for r in recommendations]

        # 构建提示词
        prompt = self._build_prompt(spots, user, context)

        # 调用LLM
        response = self.client.chat.completions.create(
            model=self.model,
            messages=[
                {"role": "system", "content": self._get_system_prompt()},
                {"role": "user", "content": prompt}
            ],
            response_format={"type": "json_object"}
        )

        # 解析结果
        llm_result = json.loads(response.choices[0].message.content)

        # 更新推荐
        for rec in recommendations:
            if rec.spot_id in llm_result:
                rec.ai_reason = llm_result[rec.spot_id].get("reason", "")
                rec.ai_tips = llm_result[rec.spot_id].get("tips", [])
                rec.score = rec.score * 0.7 + llm_result[rec.spot_id].get("score_adjustment", 0.5) * 0.3

        # 重新排序
        recommendations.sort(key=lambda x: x.score, reverse=True)

        return recommendations

    def _get_system_prompt(self) -> str:
        return """你是一个专业的旅行推荐助手。
根据用户的偏好和当前上下文，为推荐景点生成个性化的推荐理由。
输出JSON格式，key为景点ID，value包含:
- reason: 100字以内的推荐理由
- tips: 1-3个实用小贴士
- score_adjustment: 0-1的分数调整建议"""

    def _build_prompt(
        self,
        spots: List[Spot],
        user: UserProfile,
        context: RecommendationContext
    ) -> str:
        return f"""
用户偏好: {user.preference_tags}
当前位置: {context.location}
当前季节: {context.current_season}
人流偏好: {context.crowd_preference}

候选景点:
{json.dumps([{"id": s.id, "name": s.name, "tags": s.tags, "rating": s.rating} for s in spots], ensure_ascii=False)}

请为每个景点生成推荐理由。
"""
```

---

## 5. AI行程生成

### 5.1 行程生成流程

```python
class ItineraryGenerator:
    """AI行程生成器"""

    def generate(self, params: ItineraryParams) -> GeneratedItinerary:
        """
        行程生成主流程

        1. 理解用户需求 (LLM)
        2. 召回候选景点
        3. 路线优化 (约束求解)
        4. 生成详细安排 (LLM)
        5. 添加餐饮/住宿建议
        """
        # 1. 需求理解
        parsed_params = self._parse_requirements(params)

        # 2. 召回候选景点
        candidates = self._recall_spots(parsed_params)

        # 3. 路线优化
        optimized_route = self._optimize_route(candidates, parsed_params)

        # 4. 生成详细安排
        itinerary = self._generate_details(optimized_route, parsed_params)

        return itinerary

    def _parse_requirements(self, params: ItineraryParams) -> ParsedParams:
        """使用LLM解析用户需求"""
        prompt = f"""
解析以下行程需求，提取关键信息:
- 目的地: {params.destination}
- 天数: {params.days_count}
- 预算: {params.budget_level}
- 偏好: {params.travel_styles}
- 特殊要求: {params.special_requests}

输出JSON格式:
{{
    "core_themes": ["主题1", "主题2"],
    "must_have_tags": ["标签1"],
    "avoid_tags": ["标签2"],
    "daily_budget": 300,
    "pace": "relaxed/normal/intense"
}}
"""
        response = self.client.chat.completions.create(
            model="gpt-4-turbo",
            messages=[{"role": "user", "content": prompt}],
            response_format={"type": "json_object"}
        )

        return ParsedParams(**json.loads(response.choices[0].message.content))

    def _optimize_route(
        self,
        spots: List[Spot],
        params: ParsedParams
    ) -> List[DayRoute]:
        """
        路线优化

        使用约束求解 (OR-Tools / 自定义算法)

        约束条件:
        - 每天游玩时间 <= 8小时
        - 景点间移动时间最小化
        - 顺路原则
        - 避开高峰时段
        """
        # 构建距离矩阵
        distance_matrix = self._build_distance_matrix(spots)

        # 使用TSP变体求解
        solver = RouteOptimizer(
            spots=spots,
            distance_matrix=distance_matrix,
            days=params.days_count,
            max_daily_duration=8 * 60,  # 8小时(分钟)
            pace=params.pace
        )

        return solver.solve()

    def _generate_details(
        self,
        routes: List[DayRoute],
        params: ParsedParams
    ) -> GeneratedItinerary:
        """使用LLM生成详细安排"""
        prompt = f"""
为以下路线生成详细的行程安排:

天数: {len(routes)}
每日路线: {json.dumps([{"day": i+1, "spots": [s.name for s in r.spots]} for i, r in enumerate(routes)], ensure_ascii=False)}
偏好节奏: {params.pace}

请为每天安排:
1. 每个景点的建议到达时间和游玩时长
2. 午餐推荐 (当地特色)
3. 交通建议
4. 注意事项

输出JSON格式。
"""
        response = self.client.chat.completions.create(
            model="gpt-4-turbo",
            messages=[{"role": "user", "content": prompt}],
            response_format={"type": "json_object"}
        )

        return GeneratedItinerary(**json.loads(response.choices[0].message.content))
```

### 5.2 路线优化算法

```python
class RouteOptimizer:
    """路线优化器"""

    def __init__(
        self,
        spots: List[Spot],
        distance_matrix: np.ndarray,
        days: int,
        max_daily_duration: int,
        pace: str
    ):
        self.spots = spots
        self.distance_matrix = distance_matrix
        self.days = days
        self.max_daily_duration = max_daily_duration
        self.pace = pace

        # 根据节奏调整
        self.pace_factor = {
            "relaxed": 1.5,   # 多停留
            "normal": 1.0,
            "intense": 0.7    # 快速游览
        }[pace]

    def solve(self) -> List[DayRoute]:
        """
        求解最优路线

        使用贪心 + 2-opt优化
        """
        # 1. 初始化：按位置聚类
        clusters = self._cluster_spots(self.days)

        # 2. 每个聚类内TSP求解
        day_routes = []
        for cluster in clusters:
            route = self._tsp_solve(cluster)
            day_routes.append(DayRoute(spots=route))

        # 3. 检查约束，调整
        day_routes = self._adjust_routes(day_routes)

        return day_routes

    def _tsp_solve(self, spots: List[Spot]) -> List[Spot]:
        """
        TSP求解 (2-opt)

        找到访问所有景点的最短路径
        """
        n = len(spots)
        if n <= 2:
            return spots

        # 贪心初始化
        route = self._greedy_init(spots)

        # 2-opt优化
        improved = True
        while improved:
            improved = False
            for i in range(n - 1):
                for j in range(i + 2, n):
                    if self._is_better(route, i, j):
                        route = self._swap(route, i, j)
                        improved = True

        return route

    def _is_better(self, route: List[Spot], i: int, j: int) -> bool:
        """检查2-opt交换是否能改善"""
        n = len(route)
        idx_map = {s.id: idx for idx, s in enumerate(route)}

        d1 = self.distance_matrix[idx_map[route[i].id]][idx_map[route[i+1].id]]
        d2 = self.distance_matrix[idx_map[route[j].id]][idx_map[route[(j+1)%n].id]]

        d3 = self.distance_matrix[idx_map[route[i].id]][idx_map[route[j].id]]
        d4 = self.distance_matrix[idx_map[route[i+1].id]][idx_map[route[(j+1)%n].id]]

        return d3 + d4 < d1 + d2

    def _adjust_routes(self, day_routes: List[DayRoute]) -> List[DayRoute]:
        """调整路线满足时间约束"""
        for route in day_routes:
            total_duration = sum(
                s.suggested_duration * self.pace_factor
                for s in route.spots
            )

            # 超时则移除评分最低的景点
            while total_duration > self.max_daily_duration:
                min_score_spot = min(route.spots, key=lambda s: s.rating)
                route.spots.remove(min_score_spot)
                total_duration = sum(
                    s.suggested_duration * self.pace_factor
                    for s in route.spots
                )

        return day_routes
```

---

## 6. 智能问答 (RAG)

### 6.1 RAG架构

```python
class TravelRAG:
    """旅行问答RAG系统"""

    def __init__(self):
        self.embedder = OpenAIEmbeddings()
        self.vector_store = PineconeVectorStore()
        self.llm = ChatOpenAI(model="gpt-4-turbo")

    def answer(
        self,
        question: str,
        context: ConversationContext
    ) -> RAGResponse:
        """
        RAG问答流程

        1. 问题理解与改写
        2. 向量检索相关内容
        3. 构建提示词
        4. LLM生成回答
        5. 提取推荐景点
        """
        # 1. 问题改写
        rewritten = self._rewrite_query(question, context)

        # 2. 向量检索
        docs = self._retrieve(rewritten, k=5)

        # 3. 构建提示词
        prompt = self._build_rag_prompt(question, docs, context)

        # 4. LLM生成
        response = self.llm.invoke(prompt)

        # 5. 提取推荐景点
        spots = self._extract_spots(response.content)

        return RAGResponse(
            answer=response.content,
            recommended_spots=spots,
            sources=[doc.metadata for doc in docs]
        )

    def _retrieve(self, query: str, k: int = 5) -> List[Document]:
        """检索相关文档"""
        # 混合检索: 向量 + 关键词
        vector_results = self.vector_store.similarity_search(query, k=k)

        # 可选: 添加关键词检索
        # keyword_results = self.keyword_search(query, k=k)

        return vector_results

    def _build_rag_prompt(
        self,
        question: str,
        docs: List[Document],
        context: ConversationContext
    ) -> str:
        """构建RAG提示词"""
        context_str = "\n\n".join([
            f"【{doc.metadata.get('title', '参考信息')}】\n{doc.page_content}"
            for doc in docs
        ])

        return f"""
你是一个专业的旅行助手。根据以下参考信息回答用户问题。

## 用户信息
- 位置: {context.location}
- 偏好: {context.user_preferences}

## 参考信息
{context_str}

## 用户问题
{question}

## 回答要求
1. 基于参考信息回答，不要编造
2. 如果推荐景点，说明推荐理由
3. 回答简洁实用
4. 如果信息不足，诚实告知

请回答:
"""
```

### 6.2 多轮对话管理

```python
class ConversationManager:
    """对话管理器"""

    def __init__(self):
        self.sessions = {}  # session_id -> ConversationSession

    def chat(
        self,
        session_id: str,
        message: str,
        context: ConversationContext
    ) -> ChatResponse:
        """处理对话"""
        # 获取或创建会话
        session = self.sessions.get(session_id)
        if not session:
            session = ConversationSession(session_id=session_id)
            self.sessions[session_id] = session

        # 添加用户消息
        session.add_message(role="user", content=message)

        # 意图识别
        intent = self._detect_intent(message)

        # 根据意图路由
        if intent == "recommend":
            response = self._handle_recommend(message, context)
        elif intent == "plan":
            response = self._handle_plan(message, context)
        elif intent == "question":
            response = self._handle_question(message, context)
        else:
            response = self._handle_general(message, context)

        # 添加助手消息
        session.add_message(role="assistant", content=response.answer)

        return response

    def _detect_intent(self, message: str) -> str:
        """意图识别"""
        prompt = f"""
分析用户意图，返回以下之一:
- recommend: 寻求推荐
- plan: 规划行程
- question: 询问问题
- general: 闲聊/其他

用户消息: {message}

只返回意图标签:
"""
        response = self.llm.invoke(prompt)
        return response.content.strip().lower()
```

---

## 7. 实时人流预测

### 7.1 人流数据源

```python
class CrowdDataAggregator:
    """人流数据聚合器"""

    def __init__(self):
        self.data_sources = [
            GooglePopularTimesSource(),
            UserCheckinSource(),
            SocialMediaSource(),
        ]

    def get_crowd_level(self, spot_id: str) -> CrowdInfo:
        """获取实时人流信息"""
        spot = get_spot(spot_id)

        data_points = []
        for source in self.data_sources:
            try:
                data = source.fetch(spot)
                data_points.append(data)
            except Exception as e:
                log_error(e)

        # 融合数据
        crowd_level = self._aggregate(data_points)

        return CrowdInfo(
            spot_id=spot_id,
            current_level=crowd_level,
            last_updated=datetime.now(),
            forecast=self._forecast(spot_id)
        )

    def _aggregate(self, data_points: List[CrowdData]) -> str:
        """聚合多源数据"""
        if not data_points:
            return "unknown"

        # 加权平均
        scores = []
        for data in data_points:
            weight = data.reliability
            scores.append(data.normalized_score * weight)

        avg_score = sum(scores) / sum(d.reliability for d in data_points)

        if avg_score < 0.3:
            return "low"
        elif avg_score < 0.7:
            return "medium"
        else:
            return "high"

    def _forecast(self, spot_id: str) -> Dict[str, str]:
        """预测未来人流"""
        # 基于历史数据 + 当前趋势
        history = get_crowd_history(spot_id, days=30)

        # 简单规则预测
        now = datetime.now()
        forecast = {}

        for hour in [9, 12, 15, 18]:
            future_time = now.replace(hour=hour)
            historical_avg = get_historical_average(history, future_time)

            if historical_avg < 0.3:
                forecast[f"{hour}:00"] = "low"
            elif historical_avg < 0.7:
                forecast[f"{hour}:00"] = "medium"
            else:
                forecast[f"{hour}:00"] = "high"

        return forecast
```

---

## 8. 系统优化

### 8.1 缓存策略

```python
class RecommendationCache:
    """推荐结果缓存"""

    def __init__(self, redis_client):
        self.redis = redis_client

    def get_recommendations(
        self,
        user_id: str,
        context_hash: str
    ) -> Optional[List[Recommendation]]:
        """获取缓存的推荐结果"""
        key = f"rec:{user_id}:{context_hash}"
        cached = self.redis.get(key)

        if cached:
            return json.loads(cached)
        return None

    def set_recommendations(
        self,
        user_id: str,
        context_hash: str,
        recommendations: List[Recommendation],
        ttl: int = 3600  # 1小时
    ):
        """缓存推荐结果"""
        key = f"rec:{user_id}:{context_hash}"
        self.redis.setex(
            key,
            ttl,
            json.dumps([r.dict() for r in recommendations])
        )
```

### 8.2 批处理优化

```python
class BatchRecommendationProcessor:
    """批量推荐处理器"""

    async def process_batch(
        self,
        requests: List[RecommendationRequest]
    ) -> List[List[Recommendation]]:
        """
        批量处理推荐请求

        优化点:
        1. 批量向量检索
        2. 共享计算
        3. 并行处理
        """
        # 1. 批量获取用户画像
        user_ids = [r.user_id for r in requests]
        users = await batch_get_users(user_ids)

        # 2. 批量向量检索
        user_vectors = [u.embedding for u in users.values()]
        batch_results = await self.vector_db.batch_query(
            collection="spot_embeddings",
            vectors=user_vectors,
            top_k=100
        )

        # 3. 并行处理
        tasks = [
            self._process_single(request, users[request.user_id], batch_results[i])
            for i, request in enumerate(requests)
        ]

        results = await asyncio.gather(*tasks)

        return results
```

### 8.3 A/B测试框架

```python
class ABTestFramework:
    """A/B测试框架"""

    def get_experiment_variant(
        self,
        user_id: str,
        experiment_name: str
    ) -> str:
        """获取实验分组"""
        # 哈希分桶
        bucket = hash(f"{user_id}:{experiment_name}") % 100

        # 实验配置
        config = EXPERIMENTS.get(experiment_name)

        for variant, ratio in config["variants"].items():
            if bucket < ratio * 100:
                return variant
            bucket -= ratio * 100

        return "control"

    def track_metric(
        self,
        user_id: str,
        experiment_name: str,
        metric_name: str,
        value: float
    ):
        """记录实验指标"""
        variant = self.get_experiment_variant(user_id, experiment_name)

        analytics.track(
            event="experiment_metric",
            properties={
                "experiment": experiment_name,
                "variant": variant,
                "metric": metric_name,
                "value": value
            }
        )
```

---

## 9. 监控与评估

### 9.1 推荐效果指标

```python
class RecommendationMetrics:
    """推荐效果指标"""

    @staticmethod
    def calculate_metrics(
        recommendations: List[Recommendation],
        user_actions: List[UserAction]
    ) -> MetricsReport:
        """计算推荐效果"""
        rec_spot_ids = {r.spot_id for r in recommendations}

        # 点击率 (CTR)
        clicks = sum(1 for a in user_actions if a.action == "click")
        ctr = clicks / len(recommendations) if recommendations else 0

        # 转化率 (加入行程/收藏)
        conversions = sum(
            1 for a in user_actions
            if a.action in ["favorite", "add_to_itinerary"]
        )
        cvr = conversions / len(recommendations) if recommendations else 0

        # 覆盖率 (推荐多样性)
        categories = set()
        for rec in recommendations:
            spot = get_spot(rec.spot_id)
            categories.add(spot.category)
        coverage = len(categories) / TOTAL_CATEGORIES

        # NDCG (排序质量)
        ndcg = calculate_ndcg(recommendations, user_actions)

        return MetricsReport(
            ctr=ctr,
            cvr=cvr,
            coverage=coverage,
            ndcg=ndcg
        )
```

### 9.2 在线监控

```yaml
# 监控指标
metrics:
  - name: recommendation_latency
    type: histogram
    description: 推荐响应时间
    alert: p99 > 500ms

  - name: recommendation_ctr
    type: gauge
    description: 推荐点击率
    alert: < 5%

  - name: llm_call_success_rate
    type: gauge
    description: LLM调用成功率
    alert: < 99%

  - name: vector_search_latency
    type: histogram
    description: 向量检索延迟
    alert: p99 > 200ms
```

---

*文档版本: v1.0*
*更新日期: 2026-03-21*
