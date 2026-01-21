# AnimeFan MongoDB Examples

### Create

```javascript
db.anime.insertOne({
  title: "Новое аниме",
  titleEnglish: "New Anime",
  description: "Описание нового аниме для тестирования",
  genres: ["Action", "Fantasy"],
  releaseYear: 2024,
  status: "ONGOING",
  type: "TV",
  episodeCount: 12,
  rating: 0,
  ratingCount: 0,
  viewCount: 0,
  favoriteCount: 0,
  episodes: [
    { number: 1, title: "Первый эпизод", duration: 24 },
    { number: 2, title: "Второй эпизод", duration: 24 }
  ],
  createdAt: new Date(),
  updatedAt: new Date()
});

db.users.insertOne({
  username: "newuser",
  email: "newuser@example.com",
  password: "$2a$10$...", // BCrypt hash
  role: "USER",
  enabled: true,
  createdAt: new Date()
});
```

### Read

```javascript
db.anime.find({ rating: { $gt: 8 } }).sort({ rating: -1 });

db.anime.find({ genres: { $in: ["Action", "Fantasy"] } });

db.anime.find({ $text: { $search: "титан атака" } });

db.anime.find(
  { releaseYear: 2024 },
  { title: 1, rating: 1, _id: 0 }
);
```

### Update 

```javascript
db.anime.updateOne(
  { _id: ObjectId("...") },
  { $set: { rating: 9.5, ratingCount: 150 } }
);

db.anime.updateOne(
  { _id: ObjectId("...") },
  { $inc: { viewCount: 1 } }
);

db.anime.updateOne(
  { _id: ObjectId("...") },
  { $push: { episodes: { number: 3, title: "Новый эпизод", duration: 24 } } }
);

db.anime.updateOne(
  { _id: ObjectId("..."), "episodes.number": 1 },
  { $set: { "episodes.$.title": "Обновленное название" } }
);

db.anime.updateOne(
  { _id: ObjectId("...") },
  { $pull: { genres: "Horror" } }
);
```

### Delete

```javascript
db.anime.deleteOne({ _id: ObjectId("...") });

db.reviews.deleteMany({ animeId: ObjectId("...") });
```

---

## Aggregation queries

### 1. Genre statistics

```javascript
db.anime.aggregate([
  { $unwind: "$genres" },
  { $group: {
      _id: "$genres",
      animeCount: { $sum: 1 },
      avgRating: { $avg: "$rating" },
      totalViews: { $sum: "$viewCount" },
      totalFavorites: { $sum: "$favoriteCount" }
  }},
  { $sort: { animeCount: -1 } },
  { $project: {
      genre: "$_id",
      animeCount: 1,
      avgRating: { $round: ["$avgRating", 2] },
      totalViews: 1,
      totalFavorites: 1,
      _id: 0
  }}
]);
```

### 2. Top rated anime (minimum 10 ratings)

```javascript
db.anime.aggregate([
  { $match: { ratingCount: { $gte: 10 } } },
  { $sort: { rating: -1 } },
  { $limit: 10 },
  { $project: {
      title: 1,
      rating: 1,
      ratingCount: 1,
      viewCount: 1,
      genres: 1
  }}
]);
```

### 3. User activity statistics

```javascript
db.user_anime_relations.aggregate([
  { $match: { userId: "user_id_here" } },
  { $group: {
      _id: "$status",
      count: { $sum: 1 }
  }},
  { $project: {
      status: "$_id",
      count: 1,
      _id: 0
  }}
]);
```

### 4. User's genre preferences

```javascript
db.user_anime_relations.aggregate([
  { $match: { userId: "user_id_here", status: "COMPLETED" } },
  { $lookup: {
      from: "anime",
      localField: "animeId",
      foreignField: "_id",
      as: "anime"
  }},
  { $unwind: "$anime" },
  { $unwind: "$anime.genres" },
  { $group: {
      _id: "$anime.genres",
      count: { $sum: 1 },
      avgRating: { $avg: "$userRating" }
  }},
  { $sort: { count: -1 } },
  { $limit: 5 }
]);
```

### 5. Distribution of ratings for anime

```javascript
db.reviews.aggregate([
  { $match: { animeId: "anime_id_here" } },
  { $group: {
      _id: "$rating",
      count: { $sum: 1 }
  }},
  { $sort: { _id: 1 } },
  { $project: {
      rating: "$_id",
      count: 1,
      _id: 0
  }}
]);
```

### 6. Top reviewers

```javascript
db.reviews.aggregate([
  { $group: {
      _id: "$userId",
      username: { $first: "$username" },
      reviewCount: { $sum: 1 },
      avgRating: { $avg: "$rating" }
  }},
  { $sort: { reviewCount: -1 } },
  { $limit: 10 }
]);
```