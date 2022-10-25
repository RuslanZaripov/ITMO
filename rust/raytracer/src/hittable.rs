use crate::aabb::{AABB, surrounding_box};
use crate::ray::Ray;
use crate::Vec3;
use crate::material::Material;
use crate::vec3::dot;

#[derive(Copy, Clone)]
pub struct HitRecord<'a> {
    pub factor: f64,
    pub point: Vec3,
    pub normal: Vec3,
    pub material: &'a (dyn Material + 'a),
    pub front_face: bool,
    pub u: f64,
    pub v: f64,
}

pub trait Hittable {
    fn hit(&self, ray: &Ray, t_min: f64, t_max: f64) -> Option<HitRecord>;

    fn bounding_box(&self) -> Option<AABB>;

    fn get_hit_record(&self, ray: &Ray, root: f64) -> Option<HitRecord> {
        let point = ray.at(root);
        let outward_normal = self.get_normal(&point);
        let front_face = dot(&ray.dir, &outward_normal) < 0.0;
        let normal = if front_face { outward_normal } else { -outward_normal };
        let (u, v) = self.get_coordinates(&normal);
        return Some(HitRecord {
            factor: root,
            point,
            normal,
            material: self.get_material(),
            front_face,
            u,
            v
        });
    }

    fn get_normal(&self, point: &Vec3) -> Vec3;

    fn get_coordinates(&self, normal: &Vec3) -> (f64, f64);

    fn get_material(&self) -> &dyn Material;
}

pub struct HittableList {
    objects: Vec<Box<dyn Hittable>>,
}

impl HittableList {
    pub fn new() -> Self {
        Self { objects: Vec::new() }
    }

    pub fn clear(&mut self) {
        self.objects.clear();
    }

    pub fn add(&mut self, object: impl Hittable + 'static) {
        self.objects.push(Box::new(object));
    }
}

impl Hittable for HittableList {
    fn hit(&self, ray: &Ray, t_min: f64, t_max: f64) -> Option<HitRecord> {
        let mut hit_record = None;
        let mut dist_to_closest = t_max;
        for object in &self.objects {
            if let Some(rec) = object.hit(ray, t_min, dist_to_closest) {
                dist_to_closest = rec.factor;
                hit_record = Some(rec);
            }
        }
        hit_record
    }

    fn bounding_box(&self) -> Option<AABB> {
        if self.objects.is_empty() {
            return None;
        }
        let mut output_box = self.objects[0].bounding_box()?;
        for object in &self.objects {
            if let Some(container) = object.bounding_box() {
                output_box = surrounding_box(&output_box, &container);
            } else {
                return None;
            }
        }
        Some(output_box)
    }

    fn get_normal(&self, _point: &Vec3) -> Vec3 {
        panic!("HittableList::get_normal() is not implemented");
    }

    fn get_coordinates(&self, _normal: &Vec3) -> (f64, f64) {
        panic!("HittableList::get_coordinates() is not implemented");
    }

    fn get_material(&self) -> &dyn Material {
        panic!("HittableList::get_material() is not implemented");
    }
}
