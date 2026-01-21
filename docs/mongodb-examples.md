# AnimeFan MongoDB Examples

## Примеры CRUD операций

### Create (Создание)

```javascript
// Создание аниме
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

// Создание пользователя
db.users.insertOne({
  username: "newuser",
  email: "newuser@example.com",
  password: "$2a$10$...", // BCrypt hash
  role: "USER",
  enabled: true,
  createdAt: new Date()
});
```

### Read (Чтение)

```javascript
// Найти все аниме с рейтингом > 8
db.anime.find({ rating: { $gt: 8 } }).sort({ rating: -1 });

// Найти аниме по жанру
db.anime.find({ genres: { $in: ["Action", "Fantasy"] } });

// Полнотекстовый поиск
db.anime.find({ $text: { $search: "титан атака" } });

// Поиск с проекцией
db.anime.find(
  { releaseYear: 2024 },
  { title: 1, rating: 1, _id: 0 }
);
```

### Update (Обновление)

```javascript
// Обновление рейтинга (используя $set)
db.anime.updateOne(
  { _id: ObjectId("...") },
  { $set: { rating: 9.5, ratingCount: 150 } }
);

// Увеличение счетчика просмотров ($inc)
db.anime.updateOne(
  { _id: ObjectId("...") },
  { $inc: { viewCount: 1 } }
);

// Добавление эпизода ($push)
db.anime.updateOne(
  { _id: ObjectId("...") },
  { $push: { episodes: { number: 3, title: "Новый эпизод", duration: 24 } } }
);

// Обновление конкретного эпизода (позиционный оператор $)
db.anime.updateOne(
  { _id: ObjectId("..."), "episodes.number": 1 },
  { $set: { "episodes.$.title": "Обновленное название" } }
);

// Удаление элемента из массива ($pull)
db.anime.updateOne(
  { _id: ObjectId("...") },
  { $pull: { genres: "Horror" } }
);
```

### Delete (Удаление)

```javascript
// Удаление одного документа
db.anime.deleteOne({ _id: ObjectId("...") });

// Удаление нескольких документов
db.reviews.deleteMany({ animeId: ObjectId("...") });
```

---

## Агрегационные запросы

### 1. Статистика по жанрам

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

### 2. Топ аниме по рейтингу (минимум 10 оценок)

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

### 3. Статистика активности пользователя

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

### 4. Жанровые предпочтения пользователя

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

### 5. Распределение оценок для аниме

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

### 6. Топ рецензентов

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

---

## Создание индексов

```javascript
// Текстовый индекс для полнотекстового поиска
db.anime.createIndex({
  title: "text",
  description: "text",
  titleEnglish: "text",
  titleJapanese: "text"
}, {
  weights: {
    title: 3,
    titleEnglish: 2,
    description: 1,
    titleJapanese: 1
  },
  name: "anime_text_search"
});

// Составной индекс для фильтрации
db.anime.createIndex({ genres: 1, rating: -1 });
db.anime.createIndex({ studioId: 1, releaseYear: -1 });
db.anime.createIndex({ status: 1, rating: -1 });

// Уникальные индексы
db.users.createIndex({ username: 1 }, { unique: true });
db.users.createIndex({ email: 1 }, { unique: true });

// Составной уникальный индекс для отзывов
db.reviews.createIndex({ userId: 1, animeId: 1 }, { unique: true });

// Индексы для списков пользователя
db.user_anime_relations.createIndex({ userId: 1, animeId: 1 }, { unique: true });
db.user_anime_relations.createIndex({ userId: 1, status: 1 });
```

---

## Транзакции (для реплика-сета)

```javascript
const session = db.getMongo().startSession();
session.startTransaction();

try {
  // Создание отзыва
  db.reviews.insertOne({
    userId: "user1",
    animeId: "anime1",
    rating: 9,
    text: "Отличное аниме!",
    createdAt: new Date()
  }, { session });
  
  // Обновление рейтинга аниме
  const stats = db.reviews.aggregate([
    { $match: { animeId: "anime1" } },
    { $group: { _id: null, avg: { $avg: "$rating" }, count: { $sum: 1 } } }
  ], { session }).toArray()[0];
  
  db.anime.updateOne(
    { _id: ObjectId("anime1") },
    { $set: { rating: stats.avg, ratingCount: stats.count } },
    { session }
  );
  
  // Обновление счетчика отзывов пользователя
  db.users.updateOne(
    { _id: ObjectId("user1") },
    { $inc: { reviewCount: 1 } },
    { session }
  );
  
  session.commitTransaction();
} catch (error) {
  session.abortTransaction();
  throw error;
} finally {
  session.endSession();
}
```
